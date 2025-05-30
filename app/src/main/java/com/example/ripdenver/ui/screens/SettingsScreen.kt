package com.example.ripdenver.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ripdenver.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showUserIdInputDialog by remember { mutableStateOf(false) }
    var showUserIdDisplayDialog by remember { mutableStateOf(false) }
    var userIdInput by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var versionTapCount by remember { mutableStateOf(0) }
    var showTapFeedback by remember { mutableStateOf(false) }
    var tapFeedbackMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showUnsavedDialog by remember { mutableStateOf(false) }
    val hasUnsavedChanges = viewModel.hasUnsavedChanges.collectAsState().value

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importDatabase(context, it) }
    }

    fun handleBackPress() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSettings()
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Filled.Save, "Save settings")
                    }
                    IconButton(onClick = onNavigateToHelp) {
                        Icon(Icons.AutoMirrored.Filled.Help, "Guide")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Board Layout Section
            SettingsSection(
                title = "Board Settings",
                icon = Icons.Outlined.Dashboard
            ) {
                // Column Count
                SettingsSliderRow(
                    title = "Column Count",
                    value = viewModel.columnCount.value,
                    onDecrease = { viewModel.decrementColumns() },
                    onIncrease = { viewModel.incrementColumns() }
                )

                // Board Image Size
                SettingsDropdownRow(
                    title = "Image Size (Board)",
                    subtitle = "Laki ng letrato sa board",
                    value = viewModel.boardImageSize.value,
                    options = listOf("small", "medium", "large"),
                    onOptionSelected = { viewModel.setBoardImageSize(it) }
                )

                // Container Image Size
                SettingsDropdownRow(
                    title = "Image Size (Containers)",
                    subtitle = "Laki ng letrato sa containers",
                    value = viewModel.containerImageSize.value,
                    options = listOf("small", "medium", "large"),
                    onOptionSelected = { viewModel.setContainerImageSize(it) }
                )

                // Board Text Size
                SettingsDropdownRow(
                    title = "Text Size (Board)",
                    subtitle = "Laki ng text sa board",
                    value = viewModel.boardTextSize.value,
                    options = listOf("small", "medium", "large"),
                    onOptionSelected = { viewModel.setBoardTextSize(it) }
                )

                // Container Text Size
                SettingsDropdownRow(
                    title = "Text Size (Containers)",
                    subtitle = "Laki ng text sa containers",
                    value = viewModel.containerTextSize.value,
                    options = listOf("small", "medium", "large"),
                    onOptionSelected = { viewModel.setContainerTextSize(it) }
                )

                // Show Predictions switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Suggestion",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Ipakita ang mga mungkahing kard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.showPredictions.value,
                        onCheckedChange = { viewModel.togglePredictions(it) }
                    )
                }
            }
            // Prediction Section
            // Data Section
            SettingsSection(
                title = "Data Management",
                icon = Icons.Outlined.Storage
            ) {
                // Data Sharing Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Allow Data Sharing",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Pahintulutan ang pag-export ng data mula sa device na ito",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.allowDataSharing.value,
                        onCheckedChange = { viewModel.toggleDataSharing(it) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                // Export Data List Item
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Export Data",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "I-export ang data mula sa device na ito",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export Data")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                // Import Data List Item
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Import Data",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Mag-import ng datos mula sa ibang device upang ito ay mailgay sa device na ito",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Import Data")
                    }
                }
            }

            // About Section
            SettingsSection(
                title = "About",
                icon = Icons.Outlined.Info
            ) {
                // Tutorial Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHelp() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "App Tutorial",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Panoorin ang tutorial kung paano gamitin ang app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start Tutorial",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                // App Version
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            versionTapCount++
                            when (versionTapCount) {
                                1 -> {
                                    tapFeedbackMessage = "You're 4 taps away from developer tools"
                                    showTapFeedback = true
                                }
                                2 -> {
                                    tapFeedbackMessage = "You're 3 taps away from developer tools"
                                    showTapFeedback = true
                                }
                                3 -> {
                                    tapFeedbackMessage = "You're 2 taps away from developer tools"
                                    showTapFeedback = true
                                }
                                4 -> {
                                    tapFeedbackMessage = "You're 1 tap away from developer tools"
                                    showTapFeedback = true
                                }
                                5 -> {
                                    showPinDialog = true
                                    versionTapCount = 0
                                    showTapFeedback = false
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "App Version",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "v${viewModel.appVersion}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Add some bottom padding for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("May Hindi Na-save na Pagbabago") },
            text = { Text("May hindi pa na-save na pagbabago. Gusto mo bang i-save muna?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveSettings()
                        showUnsavedDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("I-save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Huwag I-save")
                    }
                    TextButton(
                        onClick = { showUnsavedDialog = false }
                    ) {
                        Text("Kanselahin")
                    }
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.exportDatabase(context)
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export as JSON File")
                    }
                    Button(
                        onClick = {
                            showExportDialog = false
                            showUserIdDisplayDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export via User ID")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // User ID Display Dialog
    if (showUserIdDisplayDialog) {
        AlertDialog(
            onDismissRequest = { showUserIdDisplayDialog = false },
            title = { Text("Your User ID") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Share this ID with others to let them import your data:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            viewModel.getUserDisplayId() ?: "Not available",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("User ID", viewModel.getUserDisplayId())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy User ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserIdDisplayDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            filePicker.launch("application/json")
                            showImportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Import from JSON File")
                    }
                    Button(
                        onClick = {
                            showImportDialog = false
                            showUserIdInputDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Import via User ID")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // User ID Input Dialog
    if (showUserIdInputDialog) {
        AlertDialog(
            onDismissRequest = { showUserIdInputDialog = false },
            title = { Text("Enter User ID") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = userIdInput,
                        onValueChange = { userIdInput = it },
                        label = { Text("User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userIdInput.isNotEmpty()) {
                            viewModel.importFromUserId(context, userIdInput)
                            showUserIdInputDialog = false
                            userIdInput = ""
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserIdInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PIN Dialog
    if (showPinDialog) {
        Log.d("SettingsScreen", "Showing PIN dialog")
        AlertDialog(
            onDismissRequest = { 
                Log.d("SettingsScreen", "PIN dialog dismissed")
                showPinDialog = false
                pinInput = ""
            },
            title = { Text("Enter Developer PIN") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = pinInput,
                        onValueChange = { 
                            Log.d("SettingsScreen", "PIN input changed: ${it.length} characters")
                            if (it.length <= 6) {
                                pinInput = it
                            }
                        },
                        label = { Text("PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("SettingsScreen", "PIN submitted: $pinInput")
                        if (pinInput == "000000") {
                            Log.d("SettingsScreen", "PIN correct, navigating to developer screen")
                            showPinDialog = false
                            pinInput = ""
                            onNavigateToDeveloper()
                        } else {
                            Log.d("SettingsScreen", "Invalid PIN entered")
                            Toast.makeText(context, "Invalid PIN", Toast.LENGTH_SHORT).show()
                            pinInput = ""
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        Log.d("SettingsScreen", "PIN dialog cancelled")
                        showPinDialog = false
                        pinInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Operation Status Dialog
    if (viewModel.showOperationStatus.value) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOperationStatus() },
            title = {
                Text(
                    if (viewModel.isOperationSuccess.value) "Success" else "Error",
                    color = if (viewModel.isOperationSuccess.value) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(viewModel.operationMessage.value)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissOperationStatus() }) {
                    Text("OK")
                }
            }
        )
    }

    // Tap Feedback Toast
    if (showTapFeedback) {
        LaunchedEffect(showTapFeedback) {
            Toast.makeText(context, tapFeedbackMessage, Toast.LENGTH_SHORT).show()
            delay(2000) // Wait for 2 seconds
            showTapFeedback = false
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
fun SettingsSliderRow(
    title: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SettingsDropdownRow(
    title: String,
    subtitle: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            Button(
                onClick = { showDropdown = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = value.capitalize(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select size",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.capitalize()) },
                        onClick = {
                            onOptionSelected(option)
                            showDropdown = false
                        }
                    )
                }
            }
        }
    }
}