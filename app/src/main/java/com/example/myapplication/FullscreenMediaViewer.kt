package com.example.myapplication
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.util.GalleryViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.rememberTransformableState

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
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        if (scale > 1f) {
            offset += panChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .then(
                // Only consume gestures when actually zoomed in
                if (scale > 1.1f) {
                    Modifier.transformable(state = transformableState)
                } else {
                    Modifier.transformable(
                        state = transformableState,
                        canPan = { false }
                    )
                }
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
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