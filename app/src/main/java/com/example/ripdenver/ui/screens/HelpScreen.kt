package com.example.ripdenver.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ripdenver.R
import kotlinx.coroutines.delay

private const val TAG = "HelpScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    showTutorial: Boolean = false
) {
    var showTutorialDialog by remember { mutableStateOf(showTutorial) }
    var currentTutorialStep by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val tutorialSteps = listOf(
        TutorialStep(
            title = "Maligayang Pagdating sa AACBAY!",
            description = "Tara at tuklasin ang mga pangunahing tampok ng app.",
            icon = Icons.Default.Info,
            imageResId = R.drawable.tutorial_welcome
        ),
        TutorialStep(
            title = "Communication Board",
            description = "Dito mo makikita ang lahat ng iyong mga card at folder. I-tap ang mga card para basahin, o i-tap ang folder para buksan.",
            icon = Icons.Default.Dashboard,
            imageResId = R.drawable.tutorial_board
        ),
        TutorialStep(
            title = "Speak Module",
            description = "Dito ka magsasalita at awtomatikong iko-convert ng app ang iyong boses sa text. Mainam ito para sa mabilis na pagbuo ng mensahe gamit ang iyong boses.",
            icon = Icons.Default.RecordVoiceOver,
            imageResId = R.drawable.tutorial_speak
        ),
        TutorialStep(
            title = "Settings",
            description = "I-personalize ang iyong karanasan dito. Baguhin ang bilang ng column, i-on o i-off ang mga mungkahi, at marami pang iba.",
            icon = Icons.Default.Settings,
            imageResId = R.drawable.tutorial_settings
        ),
        TutorialStep(
            title = "Add Card / Folder",
            description = "Gumawa ng sarili mong mga card at folder sa pamamagitan ng pag-tap sa + button. Magdagdag ng larawan, label, at ayusin ayon sa iyong gusto!",
            icon = Icons.Default.Add,
            imageResId = R.drawable.tutorial_add
        )
    )

    LaunchedEffect(showTutorial) {
        if (showTutorial) {
            showTutorialDialog = true
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "HelpScreen: LaunchedEffect triggered")
        delay(100) // Small delay to ensure logs are captured
        Log.d(TAG, "HelpScreen: Starting to compose HelpScreen")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gabay at Impormasyon") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showTutorialDialog = true }) {
                        Icon(Icons.Default.PlayArrow, "Start Tutorial")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Introduction
                ExpandableSection(
                    title = "Ano ang AAC (Augmentative and Alternative Communication)?",
                    content = "Ang Augmentative and Alternative Communication (AAC) ay mga paraan ng komunikasyon na tumutulong sa mga taong may kapansanan sa pagsasalita o wika upang makipag-usap. Maaaring ito ay sa pamamagitan ng mga kilos, larawan, simbolo, teksto, o teknolohiya tulad ng mobile applications.\n\n" +
                            "Sa AACBAY, gumagamit ang app ng communication boards at text-to-speech (TTS) upang magsalita para sa gumagamit, pati na rin ng speech-to-text (STT) para sa mga may kahirapan sa pakikinig. Gamit din ang N-gram model, nakatutulong ang app na magmungkahi ng mga salitang maaaring gamitin sa susunod, upang mas mabilis at mas episyente ang komunikasyon."
                )

                // Communication Board Module
                ExpandableSection(
                    title = "Paano gamitin ang Communication Board Module?",
                    content = "Ang Communication Board Module ay ang pangunahing bahagi ng AACBAY kung saan makikita ang mga \"card\" na naglalaman ng salita, larawan, at kulay.\n\n" +
                            "Hakbang sa paggamit:\n" +
                            "• I-tap ang isang card upang basahin ito ng app gamit ang text-to-speech.\n" +
                            "• Maaaring gumamit ng maraming cards upang makabuo ng mensahe.\n" +
                            "• Ang bawat card ay may larawan na nakakatulong sa visual na pag-unawa.\n" +
                            "• Ang mga cards ay nakaayos sa mga folder ayon sa kategorya (halimbawa: pagkain, emosyon, lugar).\n\n" +
                            "Layunin ng communication board na bigyang kakayahan ang mga gumagamit na magsalita ng mga karaniwang pangungusap o salita sa mabilis at madaling paraan."
                )

                // Speak Module
                ExpandableSection(
                    title = "Paano gamitin ang Speak Module?",
                    content = "Ang Speak Module ay bahagi ng app kung saan maaaring mag-type ng kahit anong salita o pangungusap, at ito ay babasahin ng app gamit ang text-to-speech.\n\n" +
                            "Hakbang sa paggamit:\n" +
                            "• I-type ang nais sabihin sa text box.\n" +
                            "• Makikita ang mungkahi ng salita mula sa N-gram model upang mapabilis ang pag-type.\n" +
                            "• I-tap ang \"Speak\" button upang ipabasa sa app ang teksto.\n" +
                            "• Maaari ring baguhin ang laki ng font at istilo ayon sa kagustuhan ng gumagamit.\n\n" +
                            "Ang Speak Module ay kapaki-pakinabang lalo na para sa mga nais makapagsabi ng mga salita na wala sa communication board."
                )

                // Creating Cards and Folders
                ExpandableSection(
                    title = "Paano gumawa ng bagong Cards at Folders?",
                    content = "Ang AACBAY ay nagbibigay-daan sa mga gumagamit na magdagdag ng sariling cards at folders ayon sa kanilang pangangailangan.\n\n" +
                            "Hakbang sa paggawa ng bagong card:\n" +
                            "1. Pumunta sa nais na folder.\n" +
                            "2. I-tap ang \"Add Card\" o katulad na button.\n" +
                            "3. Ilagay ang:\n" +
                            "   • Salitang nais gamitin.\n" +
                            "   • Larawan (maaaring kumuha o pumili mula sa gallery).\n" +
                            "   • Kulay ng background para sa visual na gabay.\n" +
                            "4. I-save ang card.\n\n" +
                            "Hakbang sa paggawa ng bagong folder:\n" +
                            "1. Sa main screen, i-tap ang \"Add Folder\".\n" +
                            "2. Ilagay ang pangalan ng folder (hal. \"Eskwela\" o \"Bahay\").\n" +
                            "3. Maaari ring magdagdag ng icon o larawan para madaling makilala.\n\n" +
                            "Ang kakayahang ito ay nagbibigay sa mga gumagamit ng personalized na karanasan sa app."
                )

                // Additional Information
                ExpandableSection(
                    title = "Iba pang Mahahalagang Impormasyon",
                    content = "• Koneksyon sa Internet: Kinakailangan ang aktibong koneksyon sa internet upang magamit ang AACBAY. Hindi ito gumagana nang offline.\n\n" +
                            "• Wikang Filipino: Ang app ay nakatuon sa wikang Filipino, kaya mas angkop ito sa mga lokal na gumagamit kumpara sa mga dayuhang AAC apps.\n\n" +
                            "• Libre: Ang AACBAY ay 100% libre at walang bayad.\n\n" +
                            "• Speech-to-Text: May kakayahan ang app na i-convert ang boses sa text, na mainam para sa mga may kahirapan sa pandinig.\n\n" +
                            "• Customization: Maaaring baguhin ang font style, laki, at ayos ng mga cards upang umayon sa kagustuhan ng gumagamit.\n\n" +
                            "• N-gram Suggestion: Ang app ay may word suggestion na natututo mula sa paggamit ng tao ng mga cards upang mas mapabilis ang komunikasyon."
                )
            }
        }
    }

    // Place the overlay here, outside the Scaffold
    if (showTutorialDialog) {
        TutorialOverlay(
            currentStep = currentTutorialStep,
            totalSteps = tutorialSteps.size,
            step = tutorialSteps[currentTutorialStep],
            onNext = {
                if (currentTutorialStep < tutorialSteps.size - 1) {
                    currentTutorialStep++
                } else {
                    showTutorialDialog = false
                    currentTutorialStep = 0
                }
            },
            onPrevious = {
                if (currentTutorialStep > 0) {
                    currentTutorialStep--
                }
            },
            onDismiss = {
                showTutorialDialog = false
                currentTutorialStep = 0
            }
        )
    }
}

@Composable
private fun TutorialOverlay(
    currentStep: Int,
    totalSteps: Int,
    step: TutorialStep,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .zIndex(2f),
        contentAlignment = Alignment.Center
    ) {
        // Background clickable area
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onDismiss() }
        )
        
        // Modal content
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStep == 0) {
                    // Welcome screen layout
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Other steps layout with image and content side by side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Image on the left
                        Image(
                            painter = painterResource(id = step.imageResId),
                            contentDescription = step.title,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )

                        // Content on the right
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = currentStep > 0
                    ) {
                        Text("Skip")
                    }
                    
                    Row {
                        if (currentStep > 0) {
                            TextButton(onClick = onPrevious) {
                                Text("Previous")
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                if (currentStep == totalSteps - 1) "Finish" else "Next",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / totalSteps,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Step ${currentStep + 1} of $totalSteps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val imageResId: Int
)

@Composable
private fun ExpandableSection(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        Log.d(TAG, "ExpandableSection: Composing section with title: $title")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        )
                        .padding(16.dp)
                )
            }
        }
    }
} 