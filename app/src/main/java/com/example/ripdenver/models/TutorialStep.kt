package com.example.ripdenver.models

import androidx.compose.ui.graphics.vector.ImageVector

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val imageResId: Int
) 