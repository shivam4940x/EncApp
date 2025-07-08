package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.DecryptModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TextDecryptScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var uri by remember { mutableStateOf<Uri?>(null) }
    var password by remember { mutableStateOf("") }
    var decryptedText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDecrypting by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri = it
        decryptedText = null
        errorMessage = null
    }

    fun startDecryption() {
        if (uri == null || password.isEmpty()) return
        isDecrypting = true
        scope.launch(Dispatchers.IO) {
            try {
                val result = DecryptModule.decryptFile(context, uri!!, password)
                if (result.isNotEmpty()) {
                    val file = result[0]
                    decryptedText = file.readText()
                } else {
                    errorMessage = "Decryption returned no file"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isDecrypting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                cursorColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        UploadButton("Upload Text File") {
            filePicker.launch(arrayOf("text/plain"))
        }

        Button(
            onClick = { startDecryption() },
            enabled = uri != null && password.isNotEmpty() && !isDecrypting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E4A89))
        ) {
            Text("Decrypt", color = Color.White)
        }

        if (isDecrypting) CircularProgressIndicator(color = Color.White)

        decryptedText?.let {
            Text(it, color = Color.White)
        }

        errorMessage?.let {
            Text("Error: $it", color = Color.Red)
        }
    }
}
