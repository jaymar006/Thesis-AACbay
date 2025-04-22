package com.example.ripdenver.state

data class EditCardState(
    val cardId: String = "",
    val cardLabel: String = "",
    val cardVocalization: String = "",
    val cardColor: String = "#FFFFFF",
    val cardImagePath: Pair<String, String> = Pair("", ""), // URL and Public ID
    val folderId: String = ""
)

data class EditFolderState(
    val folderId: String = "",
    val folderLabel: String = "",
    val folderColor: String = "#FFFFFF"
)