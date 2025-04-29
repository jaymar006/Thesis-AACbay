package com.example.ripdenver.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ripdenver.viewmodels.RecordViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: RecordViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isRecording = viewModel.isRecording.value
    val recognizedText = viewModel.recognizedText.value
    val fontSize = viewModel.fontSize.value
    val fontColor = viewModel.fontColor.value
    val fontFamily = viewModel.fontFamily.value
    var showLanguagePrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializeSpeechRecognizer(context)
        if (!checkFilipinoPack(context)) {
            showLanguagePrompt = true
        }
    }

    if (showLanguagePrompt) {
        LanguagePackPromptDialog(
            onDismiss = { showLanguagePrompt = false },
            onOpenSettings = {
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                } catch (e: Exception) {
                    Log.e("Settings", "Failed to open language settings", e)
                }
                showLanguagePrompt = false
            }
        )
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.toggleRecording()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Input") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier
                    .padding(0.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    ) { padding ->
        RecordingScreenContent(
            isRecording = isRecording,
            recognizedText = recognizedText,
            fontSize = fontSize,
            fontColor = fontColor,
            fontFamily = fontFamily,
            onStartListening = {
                if (!hasAudioPermission) {
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.toggleRecording()
                }
            },
            onStopListening = { viewModel.toggleRecording() },
            onFontSizeChange = { viewModel.setFontSize(it) },
            onFontColorChange = { viewModel.setFontColor(it) },
            onFontFamilyChange = { viewModel.setFontFamily(it) },
            onDismiss = {
                onDismiss()
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun LanguagePackPromptDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filipino Language Pack Needed") },
        text = { Text("For better speech recognition in Filipino, please install the Filipino language pack in your device settings.") },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

fun checkFilipinoPack(context: Context): Boolean {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil-PH")
    }

    try {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.startListening(intent)
        recognizer.stopListening()
        recognizer.destroy()
        return true
    } catch (e: Exception) {
        Log.d("SpeechRecognizer", "Filipino language not available: ${e.message}")
        return false
    }
}

@Composable
private fun RecordingScreenContent(
    isRecording: Boolean,
    recognizedText: String,
    fontSize: TextUnit,
    fontColor: Color,
    fontFamily: FontFamily,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onFontSizeChange: (TextUnit) -> Unit,
    onFontColorChange: (Color) -> Unit,
    onFontFamilyChange: (FontFamily) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LEFT SIDE: Recording controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isRecording) onStartListening()
                        else onStopListening()
                    },
                    modifier = Modifier.size(72.dp),
                    containerColor = if (isRecording)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Start recording",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isRecording) "Recording..." else "pindutin upang makapagsalita",
                    color = if (isRecording)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // RIGHT SIDE: Text display and text formatting
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                // Text display area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (recognizedText.isEmpty()) {
                            "Ang teksto na nabuo mula sa pagsasalita ay makikita dito..."
                        } else {
                            recognizedText
                        },
                        fontSize = fontSize,
                        color = fontColor,
                        fontFamily = fontFamily,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Font controls in a horizontal row
                Text(
                    text = "Baguhin and disenyo ng teksto",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font size dropdown
                    Box (modifier = Modifier.padding(horizontal = 8.dp)) {
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = !expanded }
                        ) {
                            Text("Font Size", style = MaterialTheme.typography.titleSmall)
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                "Toggle font size"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Small" to 14.sp, "Medium" to 18.sp, "Large" to 24.sp).forEach { (label, size) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        onFontSizeChange(size)
                                        expanded = false
                                    },
                                    leadingIcon = if (fontSize == size) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Font style dropdown
                    Box (modifier = Modifier.padding(horizontal = 8.dp)){
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = !expanded }
                        ) {
                            Text("Font Style", style = MaterialTheme.typography.titleSmall)
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                "Toggle font style"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(
                                "Default" to FontFamily.Default,
                                "Serif" to FontFamily.Serif,
                                "Sans Serif" to FontFamily.SansSerif
                            ).forEach { (label, family) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        onFontFamilyChange(family)
                                        expanded = false
                                    },
                                    leadingIcon = if (fontFamily == family) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Color selection
                    Box (modifier = Modifier.padding(horizontal = 8.dp)) {
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = !expanded }
                        ) {
                            Text("Font Color", style = MaterialTheme.typography.titleSmall)
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                "Toggle color"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(
                                "Black" to Color.Black,
                                "Red" to Color.Red,
                                "Blue" to Color.Blue,
                                "Green" to Color.Green
                            ).forEach { (label, color) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(color, CircleShape)
                                                    .border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Text(label)
                                        }
                                    },
                                    onClick = {
                                        onFontColorChange(color)
                                        expanded = false
                                    },
                                    leadingIcon = if (fontColor == color) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}