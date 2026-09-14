package com.example.feature.system.wifi.speedtest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.roundToInt

class SpeedTestManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val PING_URL = "https://speed.cloudflare.com/cdn-cgi/trace"
        private const val DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=15000000" // 15MB
        private const val UPLOAD_URL = "https://speed.cloudflare.com/__up"
        private const val MAX_DOWNLOAD_TIME_MS = 6000L
        private const val MAX_UPLOAD_TIME_MS = 5000L
        private const val MAX_UPLOAD_BYTES = 6_000_000L // 6MB
    }

    fun runTest(): Flow<SpeedTestState> = channelFlow {
        var state = SpeedTestState(phase = SpeedTestPhase.PING, progress = 0.05f)
        send(state)

        try {
            // --- 1. PING & JITTER ---
            val pingSamples = mutableListOf<Long>()
            for (i in 1..3) {
                if (!isActive) return@channelFlow
                val start = System.currentTimeMillis()
                val request = Request.Builder().url(PING_URL).cacheControl(CacheControl.FORCE_NETWORK).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Ping probe failed (HTTP ${response.code})")
                    response.body?.string()
                }
                val duration = System.currentTimeMillis() - start
                pingSamples.add(duration)
                state = state.copy(progress = 0.05f + (i * 0.03f))
                send(state)
            }

            val avgPing = pingSamples.average().toLong().coerceAtLeast(1)
            val jitter = if (pingSamples.size >= 2) {
                val diff1 = abs(pingSamples[1] - pingSamples[0])
                val diff2 = if (pingSamples.size > 2) abs(pingSamples[2] - pingSamples[1]) else diff1
                ((diff1 + diff2) / 2.0).toLong()
            } else 0L

            state = state.copy(
                pingMs = avgPing,
                jitterMs = jitter,
                phase = SpeedTestPhase.DOWNLOAD,
                progress = 0.15f
            )
            send(state)

            // --- 2. DOWNLOAD SPEED ---
            val downloadRequest = Request.Builder()
                .url(DOWNLOAD_URL)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()

            var totalBytesDownloaded = 0L
            val downloadStartTime = System.currentTimeMillis()
            var lastUiUpdateTime = downloadStartTime

            client.newCall(downloadRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Download test failed (HTTP ${response.code})")
                val stream = response.body?.byteStream() ?: throw IOException("Empty download body")
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int

                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) return@channelFlow
                    totalBytesDownloaded += bytesRead
                    val now = System.currentTimeMillis()
                    val elapsedMs = now - downloadStartTime

                    if (now - lastUiUpdateTime > 100 || elapsedMs >= MAX_DOWNLOAD_TIME_MS) {
                        lastUiUpdateTime = now
                        val elapsedSec = elapsedMs / 1000.0
                        if (elapsedSec > 0.05) {
                            val instantMbps = ((totalBytesDownloaded * 8.0) / (elapsedSec * 1_000_000.0))
                            val progress = 0.15f + (elapsedMs.toFloat() / MAX_DOWNLOAD_TIME_MS * 0.45f).coerceAtMost(0.45f)
                            state = state.copy(
                                currentSpeedMbps = roundToOneDecimal(instantMbps),
                                downloadMbps = roundToOneDecimal(instantMbps),
                                progress = progress
                            )
                            send(state)
                        }
                    }

                    if (elapsedMs >= MAX_DOWNLOAD_TIME_MS) {
                        break
                    }
                }
            }

            val totalDownloadElapsedSec = ((System.currentTimeMillis() - downloadStartTime) / 1000.0).coerceAtLeast(0.1)
            val finalDownloadMbps = roundToOneDecimal((totalBytesDownloaded * 8.0) / (totalDownloadElapsedSec * 1_000_000.0))

            state = state.copy(
                downloadMbps = finalDownloadMbps,
                currentSpeedMbps = 0.0,
                phase = SpeedTestPhase.UPLOAD,
                progress = 0.60f
            )
            send(state)

            // --- 3. UPLOAD SPEED ---
            val totalBytesUploaded = AtomicLong(0L)
            val uploadStartTime = System.currentTimeMillis()

            val tickerJob = launch {
                while (isActive) {
                    delay(100)
                    val now = System.currentTimeMillis()
                    val elapsed = now - uploadStartTime
                    val elapsedSec = elapsed / 1000.0
                    val uploaded = totalBytesUploaded.get()
                    if (elapsedSec > 0.05 && uploaded > 0) {
                        val instantMbps = ((uploaded * 8.0) / (elapsedSec * 1_000_000.0))
                        val progress = 0.60f + (elapsed.toFloat() / MAX_UPLOAD_TIME_MS * 0.38f).coerceAtMost(0.38f)
                        send(
                            state.copy(
                                currentSpeedMbps = roundToOneDecimal(instantMbps),
                                uploadMbps = roundToOneDecimal(instantMbps),
                                progress = progress
                            )
                        )
                    }
                }
            }

            val uploadBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                override fun contentLength() = -1L

                override fun writeTo(sink: BufferedSink) {
                    val chunk = ByteArray(16 * 1024)
                    while (totalBytesUploaded.get() < MAX_UPLOAD_BYTES && (System.currentTimeMillis() - uploadStartTime) < MAX_UPLOAD_TIME_MS) {
                        sink.write(chunk)
                        sink.flush()
                        totalBytesUploaded.addAndGet(chunk.size.toLong())
                    }
                }
            }

            val uploadRequest = Request.Builder()
                .url(UPLOAD_URL)
                .post(uploadBody)
                .build()

            try {
                client.newCall(uploadRequest).execute().use { resp ->
                    resp.code
                }
            } catch (e: Exception) {
                if (totalBytesUploaded.get() == 0L) throw e
            } finally {
                tickerJob.cancel()
            }

            val totalUploadElapsedSec = ((System.currentTimeMillis() - uploadStartTime) / 1000.0).coerceAtLeast(0.1)
            val finalUploadMbps = roundToOneDecimal((totalBytesUploaded.get() * 8.0) / (totalUploadElapsedSec * 1_000_000.0))

            state = state.copy(
                uploadMbps = finalUploadMbps,
                currentSpeedMbps = 0.0,
                phase = SpeedTestPhase.COMPLETED,
                progress = 1.0f
            )
            send(state)

        } catch (e: Exception) {
            if (!isActive) return@channelFlow
            state = state.copy(
                phase = SpeedTestPhase.ERROR,
                errorMessage = e.message ?: "Speed test encountered an error."
            )
            send(state)
        }
    }.flowOn(Dispatchers.IO)

    private fun roundToOneDecimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }
}
