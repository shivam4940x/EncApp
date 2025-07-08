package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.util.DecryptModule
import com.example.myapplication.util.GalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GalleryDecryptScreen(navController: NavController, viewModel: GalleryViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var sourceFolder by remember { mutableStateOf<Uri?>(null) }
    var isDecrypting by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val decryptedFiles = viewModel.decryptedFiles

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        sourceUris = it
        sourceFolder = null
        viewModel.clearFiles()
        errorMessage = null
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        sourceFolder = it
        sourceUris = emptyList()
        viewModel.clearFiles()
        errorMessage = null
    }

    fun startDecryption() {
        isDecrypting = true
        errorMessage = null
        scope.launch(Dispatchers.IO) {
            try {
                val result = if (sourceFolder != null) {
                    DecryptModule.decryptFolder(context, sourceFolder!!, password)
                } else {
                    sourceUris.flatMap { uri ->
                        DecryptModule.decryptFile(context, uri, password)
                    }
                }
                viewModel.setFiles(result)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
                viewModel.clearFiles()
            } finally {
                isDecrypting = false
            }
        }
    }

    if (decryptedFiles.isNotEmpty()) {
        DecryptedGrid(
            files = decryptedFiles,
            onClick = { index ->
                navController.navigate("dec/gallery/fullscreen/$index")
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF121212), Color(0xFF1E1E1E))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UploadButton("Upload Files") { filePicker.launch(arrayOf("*/*")) }
                UploadButton("Upload Folder") { folderPicker.launch(null) }
            }

            if ((sourceUris.isNotEmpty() || sourceFolder != null) && password.isNotEmpty()) {
                Button(
                    onClick = { startDecryption() },
                    enabled = !isDecrypting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E4A89))
                ) {
                    Text("Decrypt", color = Color.White)
                }
            }

            if (isDecrypting) {
                CircularProgressIndicator(color = Color.White)
            }

            errorMessage?.let {
                Text("Decryption failed: $it", color = Color.Red)
            }
        }
    }
}

@Composable
fun DecryptedGrid(files: List<File>, onClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(files) { index, file ->
            val uri = file.toUri()
            val isVideo = file.extension.lowercase() in listOf("mp4", "webm", "mkv")

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onClick(index) }
                    .background(Color.DarkGray)
            ) {
                if (isVideo) {
                    Text(
                        text = "Video: ${file.name}",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
@Composable
fun UploadButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.White)
    }
}