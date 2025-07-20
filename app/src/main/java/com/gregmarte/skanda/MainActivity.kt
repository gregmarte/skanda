package com.gregmarte.skanda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val assetManager = assets
        val folders = listOf("level1", "lunar", "solar")
        setContent {
            var selectedFolder by remember { mutableStateOf(folders.first()) }
            var images by remember { mutableStateOf(listOf<String>()) }
            var selectedImage by remember { mutableStateOf<String?>(null) }
            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            var showFullScreenImage by remember { mutableStateOf(false) }

            LaunchedEffect(selectedFolder) {
                images = assetManager.list(selectedFolder)?.toList() ?: emptyList()
                selectedImage = null
                bitmap = null
            }

            LaunchedEffect(selectedImage) {
                selectedImage?.let {
                    val input = assetManager.open("$selectedFolder/$it")
                    bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header space - this creates padding at the top of the screen
                Spacer(Modifier.height(64.dp))

                // Folder selector
                var expanded by remember { mutableStateOf(false) }
                Box (
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    Button(onClick = { expanded = true }) {
                        Text(selectedFolder)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder) },
                                onClick = {
                                    selectedFolder = folder
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Image list
                LazyColumn(
                    Modifier
                        .height(150.dp)
                        .padding(start = 16.dp)
                ) {
                    items(images) { image ->
                        Text(
                            image,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedImage = image }
                                .padding(8.dp)
                        )
                    }
                }

                // Image display
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = selectedImage,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .weight(1f)
                            .rotate(90f)
                            .clickable { showFullScreenImage = true }
                    )
                }
            }

            // Full-screen image display (conditionally shown)
            if (showFullScreenImage) {
                // State for zoom and pan for the full-screen image
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                var fullScreenRotation by remember { mutableFloatStateOf(90f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black) // Black background for full screen
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, _rotation ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offset += pan * scale
                                fullScreenRotation += _rotation
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    // Double-tap detected: close the image
                                    scale = 1f
                                    offset = Offset.Zero
                                    showFullScreenImage = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center // Center the image within the Box
                ) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(), // Use !! since we know bitmap is not null here
                        contentDescription = selectedImage,
                        modifier = Modifier
                            .fillMaxSize() // Make image fill the full screen box
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                                rotationZ = fullScreenRotation // Apply rotation here
                            }
                    )
                }
            }
        }
    }
}