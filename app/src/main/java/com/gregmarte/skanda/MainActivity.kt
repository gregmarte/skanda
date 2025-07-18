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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory

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

            Column(Modifier.padding(16.dp)) {
                // Folder selector
                var expanded by remember { mutableStateOf(false) }
                Box {
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
                LazyColumn(Modifier.height(150.dp)) {
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
                Spacer(Modifier.height(16.dp))
                // Image display
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = selectedImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }
}