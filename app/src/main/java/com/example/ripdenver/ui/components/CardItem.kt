package com.example.ripdenver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ripdenver.R
import com.example.ripdenver.models.Card
import com.example.ripdenver.utils.ImageUploader
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

@Composable
fun CardItem(
    card: Card,
    onClick: () -> Unit = {}
) {
    val imageRef = remember(card.imagePath) {
        if (card.imagePath.isNotEmpty()) {
            ImageUploader.getImageReference(card.imagePath)
        } else {
            null
        }
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(android.graphics.Color.parseColor(card.color))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (imageRef != null) {
                    FirebaseImage(
                        reference = imageRef,
                        contentDescription = card.label,
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    // Show loading indicator only in the image area
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White
                    )
                }
                // Always show label below
                Text(
                    text = card.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
                }
            }
        }
    }


@Composable
fun FirebaseImage(
    reference: StorageReference,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reference) {
        try {
            imageUrl = reference.downloadUrl.await().toString()
        } catch (e: Exception) {
            // Handle error if needed
            imageUrl = null
        }
    }

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_placeholder),
            error = painterResource(R.drawable.ic_placeholder)
        )
    } else {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
    }
}