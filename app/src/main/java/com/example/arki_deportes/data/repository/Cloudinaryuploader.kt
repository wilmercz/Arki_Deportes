package com.example.arki_deportes.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CLOUDINARY UPLOADER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Sube archivos a Cloudinary usando Upload Presets sin firma (Unsigned).
 *
 * @author ARKI SISTEMAS
 * @version 1.1.0
 */
class CloudinaryUploader(private val context: Context) {

    companion object {
        private const val CLOUD_NAME = "dm5jp6bbj"
        private const val UPLOAD_PRESET = "arki_deportes_unsigned"
        private const val TAG = "CloudinaryUploader"
        private const val ROOT_FOLDER = "ARKI_DEPORTES/CONFIGURACION"
    }

    /**
     * Sube un archivo de audio a Cloudinary.
     */
    suspend fun uploadAudioFile(fileUri: Uri, fileName: String): String {
        val safeName = fileName.substringBeforeLast(".").replace(Regex("[^A-Za-z0-9]"), "_")
        val folder = "$ROOT_FOLDER/AUDIOS"
        return uploadToCloudinary(fileUri, folder, "video", safeName)
    }

    /**
     * Sube un archivo de banner (imagen o video) a Cloudinary.
     * Corregido: carpeta de videos a BANNER_VIDEOS
     */
    suspend fun uploadBannerMedia(fileUri: Uri, folder: String, fileName: String): String {
        val cloudinaryFolder = "$ROOT_FOLDER/PUBLICIDAD/$folder"
        val resourceType = if (folder == "BANNER_VIDEOS") "video" else "image"
        
        // Sanitizar el nombre para usarlo como public_id
        val safePublicId = fileName.replace(Regex("[^A-Za-z0-9]"), "_")
        
        Log.d(TAG, "🖼️ Subiendo a: $cloudinaryFolder con ID: $safePublicId")
        return uploadToCloudinary(fileUri, cloudinaryFolder, resourceType, safePublicId)
    }

    /**
     * Motor de subida. 
     * @param publicId Si se proporciona, Cloudinary lo usará como nombre único.
     *                 Si el archivo ya existe con ese ID, se sobrescribirá/reutilizará.
     */
    private suspend fun uploadToCloudinary(
        fileUri: Uri,
        folder: String,
        resourceType: String,
        publicId: String? = null
    ): String = withContext(Dispatchers.IO) {

        val boundary = "----ArkiDeportesBoundary${System.currentTimeMillis()}"
        val apiUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/$resourceType/upload"

        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            doInput = true
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        val inputStream: InputStream = context.contentResolver.openInputStream(fileUri)
            ?: throw Exception("No se pudo abrir el archivo")

        try {
            DataOutputStream(connection.outputStream).use { output ->

                // ── Campo: upload_preset ──
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                output.writeBytes("$UPLOAD_PRESET\r\n")

                // ── Campo: folder ──
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                output.writeBytes("$folder\r\n")

                // ── Campo: public_id (CLAVE PARA EVITAR DUPLICADOS) ──
                if (publicId != null) {
                    output.writeBytes("--$boundary\r\n")
                    output.writeBytes("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n")
                    output.writeBytes("$publicId\r\n")
                }

                // ── Campo: file ──
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"file\"\r\n")
                output.writeBytes("Content-Type: application/octet-stream\r\n\r\n")

                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode == 200) connection.inputStream else connection.errorStream
            val responseBody = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

            if (responseCode != 200) {
                throw Exception("Cloudinary error $responseCode: $responseBody")
            }

            JSONObject(responseBody).getString("secure_url")
        } finally {
            inputStream.close()
            connection.disconnect()
        }
    }
}