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
 * No requiere API Secret en el cliente — es seguro para apps móviles.
 *
 * Uso:
 * ```kotlin
 * val uploader = CloudinaryUploader(context)
 * val url = uploader.uploadAudioFile(uri, "gol_fx.mp3")
 * val url = uploader.uploadBannerMedia(uri, "IMAGENES", "banner_inicio.png")
 * ```
 *
 * Configuración necesaria:
 * - Crear Upload Preset "Unsigned" en Cloudinary Console
 * - Ajustar CLOUD_NAME y UPLOAD_PRESET con tus valores
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
class CloudinaryUploader(private val context: Context) {

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN — ajusta estos valores con los tuyos
    // ═══════════════════════════════════════════════════════════════════════

    companion object {
        /** Cloud Name de tu cuenta Cloudinary */
        private const val CLOUD_NAME = "dm5jp6bbj"

        /**
         * Upload Preset de tipo "Unsigned" creado en:
         * Cloudinary Console → Settings → Upload → Upload Presets
         */
        private const val UPLOAD_PRESET = "arki_deportes_unsigned"

        private const val TAG = "CloudinaryUploader"

        /** Carpeta raíz en Cloudinary para todos los archivos de la app */
        private const val ROOT_FOLDER = "ARKI_DEPORTES/CONFIGURACION"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FUNCIÓN PÚBLICA — AUDIO
    // Reemplaza a: uploadAudioFile_Storage
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sube un archivo de audio a Cloudinary.
     *
     * Ruta en Cloudinary: ARKI_DEPORTES/CONFIGURACION/AUDIOS/{uniqueName}
     *
     * @param fileUri URI del archivo de audio seleccionado
     * @param fileName Nombre original del archivo (ej: "gol.mp3")
     * @return URL pública del archivo subido
     * @throws Exception si la subida falla
     */
    suspend fun uploadAudioFile(fileUri: Uri, fileName: String): String {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9.]"), "_")
        val folder = "$ROOT_FOLDER/AUDIOS"
        Log.d(TAG, "🎵 Iniciando subida de audio: $folder/$safeName")
        return uploadToCloudinary(fileUri, folder, "video") // "video" resource_type maneja audio también
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FUNCIÓN PÚBLICA — BANNER (imagen o video)
    // Reemplaza a: uploadBannerMedia_Storage
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sube un archivo de banner (imagen o video) a Cloudinary.
     *
     * Ruta en Cloudinary:
     * - Imágenes: ARKI_DEPORTES/CONFIGURACION/PUBLICIDAD/IMAGENES/{uniqueName}
     * - Videos:   ARKI_DEPORTES/CONFIGURACION/PUBLICIDAD/VIDEOS/{uniqueName}
     *
     * @param fileUri URI del archivo seleccionado
     * @param folder  "IMAGENES" o "VIDEOS"
     * @param fileName Nombre original del archivo
     * @return URL pública del archivo subido
     * @throws Exception si la subida falla
     */
    suspend fun uploadBannerMedia(fileUri: Uri, folder: String, fileName: String): String {
        val cloudinaryFolder = "$ROOT_FOLDER/PUBLICIDAD/$folder"
        val resourceType = if (folder == "VIDEOS") "video" else "image"
        Log.d(TAG, "🖼️ Iniciando subida de banner ($folder): $cloudinaryFolder")
        return uploadToCloudinary(fileUri, cloudinaryFolder, resourceType)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FUNCIÓN PRIVADA — MOTOR DE SUBIDA MULTIPART
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Realiza la subida real a la API REST de Cloudinary via multipart/form-data.
     *
     * Endpoint: POST https://api.cloudinary.com/v1_1/{cloud}/{resourceType}/upload
     *
     * @param fileUri      URI del archivo a subir
     * @param folder       Carpeta destino en Cloudinary
     * @param resourceType "image", "video" (audio también usa "video")
     * @return secure_url del archivo subido
     */
    private suspend fun uploadToCloudinary(
        fileUri: Uri,
        folder: String,
        resourceType: String
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
            ?: throw Exception("No se pudo abrir el archivo desde la URI proporcionada")

        try {
            DataOutputStream(connection.outputStream).use { output ->

                // ── Campo: upload_preset ──────────────────────────────
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                output.writeBytes("$UPLOAD_PRESET\r\n")

                // ── Campo: folder ─────────────────────────────────────
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                output.writeBytes("$folder\r\n")

                // ── Campo: file (binario) ─────────────────────────────
                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"upload\"\r\n"
                )
                output.writeBytes("Content-Type: application/octet-stream\r\n\r\n")

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
            }

            // ── Leer respuesta ────────────────────────────────────────
            val responseCode = connection.responseCode
            val responseStream = if (responseCode == 200) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = BufferedReader(InputStreamReader(responseStream)).use { reader ->
                reader.readText()
            }

            Log.d(TAG, "📡 Cloudinary response ($responseCode): $responseBody")

            if (responseCode != 200) {
                throw Exception("Cloudinary error $responseCode: $responseBody")
            }

            val json = JSONObject(responseBody)
            val secureUrl = json.getString("secure_url")

            Log.d(TAG, "✅ Subida exitosa: $secureUrl")
            secureUrl

        } finally {
            inputStream.close()
            connection.disconnect()
        }
    }
}