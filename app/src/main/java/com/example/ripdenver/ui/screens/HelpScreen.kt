package com.example.ripdenver.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val TAG = "HelpScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
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
                }
            )
        }
    ) { padding ->
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
                    .clickable { 
                        Log.d(TAG, "ExpandableSection: Toggling expanded state for: $title")
                        expanded = !expanded 
                    }
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