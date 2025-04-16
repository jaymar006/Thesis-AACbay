package com.example.ripdenver.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ripdenver.R
import com.example.ripdenver.models.Card

@Composable
fun CardItem(
    card: Card,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

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
                if (card.cloudinaryUrl.isNotEmpty()) {
                    println("Loading image from URL: ${card.cloudinaryUrl}")
                    // Load image using Glide
                    Glide.with(context)
                        .asBitmap()
                        .load(card.cloudinaryUrl)
                        .placeholder(R.drawable.ic_placeholder)  // Add a placeholder drawable
                        .error(R.drawable.ic_placeholder)  // Add an error drawable
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                bitmap = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                bitmap = null
                            }
                        })

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = card.label,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Loading",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No image",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
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