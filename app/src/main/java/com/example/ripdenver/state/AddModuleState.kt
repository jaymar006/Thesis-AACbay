package com.example.ripdenver.state

data class AddModuleState(
    val isCardSelected: Boolean = true,
    val cardLabel: String = "",
    val cardVocalization: String = "",
    val cardColor: String = "#FF0000",
    val cardImagePath: String = "",
    val folderLabel: String = "",
    val folderColor: String = "#6200EE",
    val folderImagePath: String = ""
)