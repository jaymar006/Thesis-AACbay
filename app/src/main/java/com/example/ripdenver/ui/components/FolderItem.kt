package com.example.ripdenver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ripdenver.models.Folder


@Composable
fun FolderItem(
    folder: Folder,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Folder "tab" at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.4f)
                .aspectRatio(2f)
                .background(
                    color = Color(android.graphics.Color.parseColor(folder.color))
                        .copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.extraSmall.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    )
                )
                .padding(top = 8.dp)
        )

        // Main folder body
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            shape = MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(4.dp),
                topEnd = CornerSize(10.dp)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(android.graphics.Color.parseColor(folder.color))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    maxLines = 2
                )
            }
        }
    }
}