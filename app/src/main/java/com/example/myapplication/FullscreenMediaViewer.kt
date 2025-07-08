package com.example.myapplication
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.util.GalleryViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

@Composable
fun FullscreenMediaViewer(
    index: Int,
    navController: NavController,
    viewModel: GalleryViewModel
) {
    val files = viewModel.decryptedFiles
    val pagerState = rememberPagerState(
        initialPage = index,
        pageCount = { files.size }
    )

    BackHandler { navController.popBackStack() }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { page ->
        val file = files[page]
        val uri = file.toUri()
        val isVideo = file.extension.lowercase() in listOf("mp4", "webm", "mkv")

        if (isVideo) {
            VideoPlayer(uri = uri)
        } else {
            ZoomableImage(uri = uri)
        }
    }
}

@Composable
fun ZoomableImage(uri: Uri) {
    key(uri) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
                .background(Color.Black)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            VideoView(context).apply {
                setVideoURI(uri)
                setMediaController(MediaController(context).apply {
                    setAnchorView(this@apply)
                })
                setOnPreparedListener { start() }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}