package com.example.ripdenver.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ripdenver.R
import com.example.ripdenver.models.Card

@Composable
fun CardItem(
    card: Card,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cardColor = Color(android.graphics.Color.parseColor(card.color))

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .border(
                width = 1.dp,
                color = cardColor,
                shape = MaterialTheme.shapes.medium
            ),
        onClick = onClick
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            val imageSize = maxWidth * 0.5f // half the width
            val fontSize = maxWidth.value * 0.12f // scale font size

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (card.cloudinaryUrl.isNotEmpty()) {
                    Glide.with(context)
                        .asBitmap()
                        .load(card.cloudinaryUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
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
                            modifier = Modifier.size(imageSize),
                            contentScale = ContentScale.Fit
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Loading",
                        tint = Color.White,
                        modifier = Modifier.size(imageSize * 0.6f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No image",
                        tint = Color.White,
                        modifier = Modifier.size(imageSize * 0.6f)
                    )
                }

                Text(
                    text = card.label.split(" ").joinToString(" ") { word ->
                        if (word == "I") word else word.lowercase()
                    },
                    modifier = Modifier.padding(2.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = fontSize.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}