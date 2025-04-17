package com.example.ripdenver.state

data class AddModuleState(
    val isCardSelected: Boolean = true,
    val cardLabel: String = "",
    val cardVocalization: String = "",
    val cardColor: String = "#FF0000",
    val cardImagePath: Pair<String, String> = Pair("", ""), // URL and PublicID
    val folderLabel: String = "",
    val folderColor: String = "#FF0000",
    val folderId: String = ""
)