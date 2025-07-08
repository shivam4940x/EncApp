package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object EncryptModule {
    private const val GCM_NONCE_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    fun encryptFilesFromUri(
        context: Context,
        sourceFolderUri: Uri,
        destFolderUri: Uri,
        password: String
    ) {
        val key = deriveKeyFromPassword(password)

        val sourceFolder = DocumentFile.fromTreeUri(context, sourceFolderUri)
        val destFolder = DocumentFile.fromTreeUri(context, destFolderUri)

        if (sourceFolder == null || destFolder == null) {
            Log.e("Encrypt", "Invalid source or destination folder URI")
            return
        }

        sourceFolder.listFiles().forEach { file ->
            if (file.isFile) {
                val inputStream = context.contentResolver.openInputStream(file.uri)
                val fileName = file.name ?: return@forEach
                val encryptedName = "$fileName.enc"
                val outputFile = destFolder.createFile("application/octet-stream", encryptedName)
                val outputStream = outputFile?.uri?.let {
                    context.contentResolver.openOutputStream(it)
                }

                if (inputStream != null && outputStream != null) {
                    try {
                        encryptStream(inputStream, outputStream, key)
                        Log.d("Encrypt", "Encrypted: $fileName")
                    } catch (e: Exception) {
                        Log.e("Encrypt", "Failed to encrypt $fileName", e)
                    }
                }
            }
        }
    }

    fun encryptSingleFile(
        context: Context,
        sourceUri: Uri,
        destFolderUri: Uri,
        password: String
    ) {
        val key = deriveKeyFromPassword(password)

        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val fileName = DocumentFile.fromSingleUri(context, sourceUri)?.name ?: "unknown"
        val destFolder = DocumentFile.fromTreeUri(context, destFolderUri)

        if (inputStream == null || destFolder == null) {
            Log.e("Encrypt", "Invalid input stream or destination folder")
            return
        }

        val outputFile = destFolder.createFile("application/octet-stream", "$fileName.enc")
        val outputStream = outputFile?.uri?.let {
            context.contentResolver.openOutputStream(it)
        }

        if (outputStream != null) {
            try {
                encryptStream(inputStream, outputStream, key)
                Log.d("Encrypt", "Encrypted file: $fileName")
            } catch (e: Exception) {
                Log.e("Encrypt", "Failed to encrypt $fileName", e)
            }
        }
    }

    private fun deriveKeyFromPassword(password: String): ByteArray {
        val sha256 = MessageDigest.getInstance("SHA-256")
        return sha256.digest(password.toByteArray())
    }

    private fun encryptStream(input: InputStream, output: OutputStream, key: ByteArray) {
        val nonce = Random.Default.nextBytes(GCM_NONCE_LENGTH)
        output.write(nonce) // Prepend nonce

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherOut = CipherOutputStream(output, cipher)

        val buffer = ByteArray(4096)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            cipherOut.write(buffer, 0, len)
        }

        cipherOut.flush()
        cipherOut.close()
        input.close()
    }
}
