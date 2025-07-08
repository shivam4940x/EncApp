package com.example.myapplication.util

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import java.io.File

class GalleryViewModel : ViewModel() {
    var decryptedFiles by mutableStateOf<List<File>>(emptyList())
        private set

    fun setFiles(files: List<File>) {
        decryptedFiles = files
    }

    fun clearFiles() {
        decryptedFiles = emptyList()
    }
}
