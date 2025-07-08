package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object DecryptModule {
    private const val GCM_NONCE_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    fun decryptFolder(context: Context, folderUri: Uri, password: String): List<File> {
        val key = deriveKeyFromPassword(password)
        val cacheDir = File(context.cacheDir, "decrypted").apply { mkdirs() }
        val outputFiles = mutableListOf<File>()

        val sourceFolder = DocumentFile.fromTreeUri(context, folderUri)
        sourceFolder?.listFiles()?.forEach { file ->
            try {
                if (file.name?.endsWith(".enc") == true && file.isFile) {
                    val inputStream = context.contentResolver.openInputStream(file.uri) ?: return@forEach
                    val outputFile = File(cacheDir, file.name!!.removeSuffix(".enc"))
                    val outputStream = outputFile.outputStream()

                    Log.d("Decrypt", "Decrypting file: ${file.name}")
                    decryptStream(inputStream, outputStream, key)
                    outputFiles.add(outputFile)
                    Log.d("Decrypt", "Decryption success: ${outputFile.name}")
                }
            } catch (e: Exception) {
                Log.e("Decrypt", "Failed to decrypt file ${file.name}: ${e.message}")
                e.printStackTrace()
            }
        }

        return outputFiles
    }

    fun decryptFile(context: Context, fileUri: Uri, password: String): List<File> {
        val key = deriveKeyFromPassword(password)
        val cacheDir = File(context.cacheDir, "decrypted").apply { mkdirs() }

        val name = DocumentFile.fromSingleUri(context, fileUri)?.name ?: "output"
        if (!name.endsWith(".enc")) return emptyList()

        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return emptyList()
            val outputFile = File(cacheDir, name.removeSuffix(".enc"))
            val outputStream = outputFile.outputStream()

            Log.d("Decrypt", "Decrypting single file: $name")
            decryptStream(inputStream, outputStream, key)
            Log.d("Decrypt", "Decryption success: ${outputFile.name}")

            listOf(outputFile)
        } catch (e: Exception) {
            Log.e("Decrypt", "Failed to decrypt file $name: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun deriveKeyFromPassword(password: String): ByteArray {
        val sha256 = MessageDigest.getInstance("SHA-256")
        return sha256.digest(password.toByteArray())
    }

    private fun decryptStream(input: InputStream, output: OutputStream, key: ByteArray) {
        try {
            val nonce = ByteArray(GCM_NONCE_LENGTH)
            val readBytes = input.read(nonce)
            require(readBytes == GCM_NONCE_LENGTH) { "Nonce not fully read, got $readBytes bytes" }

            val encryptedData = input.readBytes()

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(key, "AES")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decrypted = cipher.doFinal(encryptedData)
            output.write(decrypted)
        } catch (e: Exception) {
            Log.e("Decrypt", "Stream decryption failed: ${e.message}")
            throw e
        } finally {
            input.close()
            output.close()
        }
    }
}
