package com.example.myapplication

import android.content.Context
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.EncryptModule
import androidx.compose.material3.TextFieldDefaults

@Composable
fun EncryptScreen(context: Context = LocalContext.current) {
    var sourceUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var sourceFolder by remember { mutableStateOf<Uri?>(null) }
    var destFolder by remember { mutableStateOf<Uri?>(null) }
    var selectingMode by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        sourceUris = it
        selectingMode = "file"
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        sourceFolder = it
        selectingMode = "folder"
    }

    val destPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        destFolder = it
        if (it != null && password.isNotBlank()) {
            when (selectingMode) {
                "file" -> sourceUris.forEach { uri ->
                    EncryptModule.encryptSingleFile(context, uri, it, password)
                }
                "folder" -> sourceFolder?.let { folder ->
                    EncryptModule.encryptFilesFromUri(context, folder, it, password)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
                )
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1C))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Encrypt Files", style = MaterialTheme.typography.headlineSmall, color = Color.White)

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransparentUploadButton("File(s)", modifier = Modifier.weight(1f)) {
                    filePicker.launch(arrayOf("*/*"))
                }
                TransparentUploadButton("Folder", modifier = Modifier.weight(1f)) {
                    folderPicker.launch(null)
                }
            }

            if (sourceUris.isNotEmpty() || sourceFolder != null) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)
                TransparentUploadButton("Select Destination & Encrypt") {
                    destPicker.launch(null)
                }
            }
        }
    }
}

@Composable
fun TransparentUploadButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.White)
    }
}
