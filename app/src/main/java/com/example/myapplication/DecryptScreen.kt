package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun DecryptScreen(navController: NavHostController) {
    var tappedButton by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Color.White.copy(
                        alpha = if (tappedButton == "text") 0.3f else 0.1f
                    )
                )
                .clickable {
                    tappedButton = "text"
                    navController.navigate("dec/text")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Text",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Color.White.copy(
                        alpha = if (tappedButton == "gallery") 0.3f else 0.1f
                    )
                )
                .clickable {
                    tappedButton = "gallery"
                    navController.navigate("dec/gallery")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}
