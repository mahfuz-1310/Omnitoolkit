package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    ORIGINAL("original", "image/*", "Original Format"),
    JPEG("jpg", "image/jpeg", "JPEG (.jpg)"),
    PNG("png", "image/png", "PNG (.png)"),
    WEBP("webp", "image/webp", "WebP (.webp)")
}

data class ExifMetadata(
    val make: String?,
    val model: String?,
    val dateTimeOriginal: String?,
    val width: String?,
    val height: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val hasGps: Boolean = false,
    val software: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val exposureTime: String? = null,
    val fNumber: String? = null
)

data class SaveResult(
    val uri: Uri,
    val displayName: String,
    val relativePath: String,
    val mimeType: String,
    val fileSize: Long = 0L
)

object ExifEditorUtils {

    fun getOriginalFileName(context: Context, uri: Uri): String {
        var name = "image_${System.currentTimeMillis()}.jpg"
        try {
            val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        val str = it.getString(index)
                        if (!str.isNullOrBlank()) {
                            name = str
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return name
    }

    fun readMetadata(context: Context, uri: Uri): ExifMetadata {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                val dt = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
                val height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
                val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
                val iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
                val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)

                val latLong = FloatArray(2)
                val hasGps = exif.getLatLong(latLong)
                val latitude = if (hasGps) latLong[0].toDouble() else null
                val longitude = if (hasGps) latLong[1].toDouble() else null

                inputStream.close()
                ExifMetadata(
                    make = make,
                    model = model,
                    dateTimeOriginal = dt,
                    width = width,
                    height = height,
                    latitude = latitude,
                    longitude = longitude,
                    hasGps = hasGps,
                    software = software,
                    iso = iso,
                    focalLength = focalLength,
                    exposureTime = exposureTime,
                    fNumber = fNumber
                )
            } else {
                ExifMetadata(null, null, null, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ExifMetadata(null, null, null, null, null)
        }
    }

    fun saveEditedImageToGallery(
        context: Context,
        sourceUri: Uri,
        customFileName: String,
        subFolder: String = "Image Metadata Editor",
        format: ExportFormat = ExportFormat.ORIGINAL,
        quality: Int = 95,
        keepLossless: Boolean = true,
        removeAllMetadata: Boolean = false,
        removeLocation: Boolean = false,
        removeDevice: Boolean = false,
        newDateTime: String? = null,
        newMake: String? = null,
        newModel: String? = null
    ): SaveResult? {
        var tempFile: File? = null
        try {
            val originalMime = context.contentResolver.getType(sourceUri) ?: "image/jpeg"
            val targetMime = when (format) {
                ExportFormat.ORIGINAL -> if (originalMime.contains("png", true)) "image/png" else if (originalMime.contains("webp", true)) "image/webp" else "image/jpeg"
                ExportFormat.JPEG -> "image/jpeg"
                ExportFormat.PNG -> "image/png"
                ExportFormat.WEBP -> "image/webp"
            }

            val targetExtension = when (targetMime) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }

            var cleanFileName = customFileName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (cleanFileName.isBlank()) {
                cleanFileName = "img_edited_${System.currentTimeMillis()}"
            }
            if (!cleanFileName.endsWith(targetExtension, ignoreCase = true)) {
                cleanFileName = cleanFileName.substringBeforeLast(".") + targetExtension
            }

            val cleanSubFolder = subFolder.trim().replace(Regex("[\\\\:*?\"<>|]"), "_").ifBlank { "Image Metadata Editor" }

            // 1. Create a working temporary file in cache directory
            tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}$targetExtension")

            val isSameAsOriginalFormat = targetMime.equals(originalMime, ignoreCase = true)
            val isJpeg = targetMime == "image/jpeg"

            if (isSameAsOriginalFormat && isJpeg && keepLossless) {
                // Direct stream copy without re-encoding to preserve 100% original image quality
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return null
            } else {
                // Decode bitmap and compress with specified format and quality
                val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return null

                val compressFormat = when (targetMime) {
                    "image/png" -> Bitmap.CompressFormat.PNG
                    "image/webp" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                    }
                    else -> Bitmap.CompressFormat.JPEG
                }

                tempFile.outputStream().use { output ->
                    bitmap.compress(compressFormat, quality.coerceIn(10, 100), output)
                }
                bitmap.recycle()
            }

            // 2. Modify EXIF metadata if target format supports EXIF (primarily JPEG)
            if (isJpeg) {
                try {
                    val exif = ExifInterface(tempFile.absolutePath)
                    val gpsTags = listOf(
                        ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE, ExifInterface.TAG_GPS_LATITUDE_REF,
                        ExifInterface.TAG_GPS_LONGITUDE_REF, ExifInterface.TAG_GPS_ALTITUDE_REF,
                        ExifInterface.TAG_GPS_DATESTAMP, ExifInterface.TAG_GPS_TIMESTAMP,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_GPS_SATELLITES,
                        ExifInterface.TAG_GPS_STATUS, ExifInterface.TAG_GPS_MEASURE_MODE,
                        ExifInterface.TAG_GPS_DOP, ExifInterface.TAG_GPS_SPEED_REF,
                        ExifInterface.TAG_GPS_SPEED, ExifInterface.TAG_GPS_TRACK_REF,
                        ExifInterface.TAG_GPS_TRACK, ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
                        ExifInterface.TAG_GPS_IMG_DIRECTION, ExifInterface.TAG_GPS_MAP_DATUM,
                        ExifInterface.TAG_GPS_DEST_LATITUDE_REF, ExifInterface.TAG_GPS_DEST_LATITUDE,
                        ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, ExifInterface.TAG_GPS_DEST_LONGITUDE,
                        ExifInterface.TAG_GPS_DEST_BEARING_REF, ExifInterface.TAG_GPS_DEST_BEARING,
                        ExifInterface.TAG_GPS_DEST_DISTANCE_REF, ExifInterface.TAG_GPS_DEST_DISTANCE,
                        ExifInterface.TAG_GPS_AREA_INFORMATION
                    )

                    if (removeAllMetadata) {
                        val tagsToClear = gpsTags + listOf(
                            ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL,
                            ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_DATETIME,
                            ExifInterface.TAG_DATETIME_DIGITIZED,
                            ExifInterface.TAG_ARTIST, ExifInterface.TAG_SOFTWARE,
                            ExifInterface.TAG_USER_COMMENT, ExifInterface.TAG_COPYRIGHT,
                            ExifInterface.TAG_IMAGE_DESCRIPTION, ExifInterface.TAG_F_NUMBER,
                            ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_ISO_SPEED_RATINGS,
                            ExifInterface.TAG_FOCAL_LENGTH, ExifInterface.TAG_LENS_MAKE,
                            ExifInterface.TAG_LENS_MODEL
                        )
                        for (tag in tagsToClear) {
                            exif.setAttribute(tag, null)
                        }
                    } else {
                        if (removeLocation) {
                            for (tag in gpsTags) {
                                exif.setAttribute(tag, null)
                            }
                        }

                        if (removeDevice) {
                            exif.setAttribute(ExifInterface.TAG_MAKE, null)
                            exif.setAttribute(ExifInterface.TAG_MODEL, null)
                        } else {
                            if (!newMake.isNullOrBlank()) {
                                exif.setAttribute(ExifInterface.TAG_MAKE, newMake)
                            }
                            if (!newModel.isNullOrBlank()) {
                                exif.setAttribute(ExifInterface.TAG_MODEL, newModel)
                            }
                        }

                        if (!newDateTime.isNullOrBlank()) {
                            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, newDateTime)
                            exif.setAttribute(ExifInterface.TAG_DATETIME, newDateTime)
                            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, newDateTime)
                        }
                    }

                    exif.saveAttributes()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val relativePath = "${Environment.DIRECTORY_PICTURES}/Arw Hyper Toolkit/$cleanSubFolder"

            // 3. Save into public gallery-visible storage via MediaStore (API 29+) or external storage + MediaScanner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, cleanFileName)
                    put(MediaStore.Images.Media.MIME_TYPE, targetMime)
                    put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val contentUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                try {
                    context.contentResolver.openOutputStream(contentUri)?.use { output ->
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    } ?: run {
                        context.contentResolver.delete(contentUri, null, null)
                        return null
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(contentUri, contentValues, null, null)

                    MediaScannerConnection.scanFile(context, arrayOf(contentUri.toString()), arrayOf(targetMime), null)

                    return SaveResult(
                        uri = contentUri,
                        displayName = cleanFileName,
                        relativePath = "$relativePath/$cleanFileName",
                        mimeType = targetMime,
                        fileSize = tempFile.length()
                    )
                } catch (e: Exception) {
                    context.contentResolver.delete(contentUri, null, null)
                    throw e
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appFolder = File(picturesDir, "Arw Hyper Toolkit/$cleanSubFolder")
                if (!appFolder.exists()) {
                    appFolder.mkdirs()
                }
                val destFile = File(appFolder, cleanFileName)
                tempFile.copyTo(destFile, overwrite = true)

                var resultUri: Uri = Uri.fromFile(destFile)
                MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf(targetMime)) { _, uri ->
                    if (uri != null) {
                        resultUri = uri
                    }
                }

                return SaveResult(
                    uri = resultUri,
                    displayName = cleanFileName,
                    relativePath = "$relativePath/$cleanFileName",
                    mimeType = targetMime,
                    fileSize = destFile.length()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            tempFile?.delete()
        }
    }

    fun openInGallery(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with Gallery"))
        } catch (e: Exception) {
            Toast.makeText(context, "No gallery app found to open image", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareImage(context: Context, uri: Uri, mimeType: String = "image/*") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Exported Image"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share image", Toast.LENGTH_SHORT).show()
        }
    }
}
