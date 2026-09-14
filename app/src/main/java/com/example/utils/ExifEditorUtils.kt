package com.example.utils

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

data class ExifMetadata(
    val make: String?,
    val model: String?,
    val dateTimeOriginal: String?,
    val width: String?,
    val height: String?
)

object ExifEditorUtils {
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
                inputStream.close()
                ExifMetadata(make, model, dt, width, height)
            } else {
                ExifMetadata(null, null, null, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ExifMetadata(null, null, null, null, null)
        }
    }

    fun saveEditedImage(
        context: Context,
        sourceUri: Uri,
        removeAllMetadata: Boolean,
        removeLocation: Boolean,
        removeDevice: Boolean,
        newDateTime: String?,
        newMake: String?,
        newModel: String?
    ): Uri? {
        return try {
            val fileName = "img_edited_${System.currentTimeMillis()}.jpg"
            val outputFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val exif = ExifInterface(outputFile.absolutePath)

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
                }
            }

            exif.saveAttributes()
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

