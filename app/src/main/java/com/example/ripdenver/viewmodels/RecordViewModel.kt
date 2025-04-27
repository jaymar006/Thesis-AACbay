package com.example.ripdenver.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor() : ViewModel() {
    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    private val _fontSize = mutableStateOf(16.sp)
    val fontSize: State<TextUnit> = _fontSize

    private val _fontColor = mutableStateOf(Color.Black)
    val fontColor: State<Color> = _fontColor

    private val _fontFamily = mutableStateOf<FontFamily>(FontFamily.Default)
    val fontFamily: State<FontFamily> = _fontFamily

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setRecognizedText(text: String) {
        _recognizedText.value = text
    }

    fun setFontSize(size: TextUnit) {
        _fontSize.value = size
    }

    fun setFontColor(color: Color) {
        _fontColor.value = color
    }

    fun setFontFamily(family: FontFamily) {
        _fontFamily.value = family
    }
}