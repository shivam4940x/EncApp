package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.util.GalleryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val galleryViewModel: GalleryViewModel = viewModel()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController) }
                    composable("main") { MainScreen(navController) }
                    composable("enc") { EncryptScreen() }
                    composable("dec") { DecryptScreen(navController) }
                    composable("dec/text") { TextDecryptScreen() }
                    composable("dec/gallery") {
                        GalleryDecryptScreen(navController = navController, viewModel = galleryViewModel)
                    }
                    composable(
                        "dec/gallery/fullscreen/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        FullscreenMediaViewer(index = index, navController = navController, viewModel = galleryViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    var tapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                tapCount++
                if (tapCount >= 3) {
                    navController.navigate("main")
                    tapCount = 0 // reset if returning
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🚧 Under Construction 🚧",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,

            modifier = Modifier.matchParentSize()    .blur(16.dp)
        )

        Column(
            modifier = Modifier
                .background(
                    Color.Transparent
                )
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))

                    .background(Color.White.copy(alpha = if (isPressed) 0.3f else 0.1f))
                    .clickable {
                        isPressed = true
                        navController.navigate("enc")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Encrypt",
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
                    .background(Color.White.copy(alpha = if (isPressed) 0.3f else 0.1f))
                    .clickable {
                        isPressed = true
                        navController.navigate("dec")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Decrypt",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
    }
}

