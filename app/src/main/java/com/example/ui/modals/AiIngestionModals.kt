package com.example.ui.modals

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.ai.GeminiExpenseAiService
import com.example.data.ai.ParsedExpenseDraft
import com.example.data.local.CategoryEntity
import com.example.ui.AiExtractionState
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCrimson
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaIndigoContainer
import com.example.ui.theme.LuminaIndigoLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceQuickLogModal(
    extractionState: AiExtractionState,
    categories: List<CategoryEntity>,
    currencyCode: String,
    onParseText: (String, String) -> Unit,
    onConfirmSave: (
        merchant: String,
        amount: Double,
        dateString: String,
        categoryName: String,
        note: String,
        tagsCsv: String,
        taxAmount: Double,
        lineItems: String,
        isSubscription: Boolean,
        sentimentNote: String,
        sourceType: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var transcriptText by rememberSaveable {
        mutableStateOf("Spent 350 rupees on dinner with friends at Paradise Biryani")
    }
    var isListening by remember { mutableStateOf(false) }
    var speechStatus by remember { mutableStateOf("Tap Mic to speak or edit conversational note below") }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                speechStatus = "Listening... Speak your expense naturally"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                speechStatus = "Voice capture finished or unavailable on emulator — use text or presets below"
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull()
                if (!spoken.isNullOrBlank()) {
                    transcriptText = spoken
                    speechStatus = "Captured voice transcript!"
                    onParseText(spoken, "VOICE_AI")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    transcriptText = partial
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(listener)
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    fun startVoiceListening() {
        if (speechRecognizer == null) {
            speechStatus = "Speech recognition service not found on this device; use smart text input below."
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            speechStatus = "Could not start microphone: ${e.localizedMessage}"
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceListening()
        } else {
            speechStatus = "Microphone permission denied — type or select a voice preset below."
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LuminaIndigoContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = LuminaIndigoLight
                    )
                }
                Column {
                    Text(
                        text = "Voice Quick-Log & Smart AI Parser",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = speechStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isListening) LuminaEmerald else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Trigger + Conversational Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = {
                        if (isListening) {
                            speechRecognizer?.stopListening()
                            isListening = false
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                startVoiceListening()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    shape = CircleShape,
                    color = if (isListening) LuminaCrimson else LuminaIndigo,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("mic_record_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Toggle Voice Recording",
                            tint = Color.White
                        )
                    }
                }

                OutlinedTextField(
                    value = transcriptText,
                    onValueChange = { transcriptText = it },
                    label = { Text("Spoken or Natural Language Expense") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminaIndigo,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedContainerColor = ObsidianCard,
                        unfocusedContainerColor = ObsidianCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_transcript_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Sample Voice Phrases
            Text(
                text = "Try sample conversational inputs:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    "Spent 350 rupees on dinner with friends at Paradise Biryani",
                    "Fuel at Indian Oil 1200",
                    "Monthly Netflix subscription 649",
                    "Bought medicines at Apollo Pharmacy for 890"
                )
                presets.forEachIndexed { idx, phrase ->
                    Surface(
                        onClick = {
                            transcriptText = phrase
                            onParseText(phrase, "VOICE_AI")
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.testTag("voice_preset_$idx")
                    ) {
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onParseText(transcriptText, "VOICE_AI") },
                enabled = transcriptText.isNotBlank() && extractionState !is AiExtractionState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuminaIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("extract_with_gemini_button")
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Extract Structured Fields with Gemini AI",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extraction State Output / Editable Confirmation
            AiExtractionReviewSection(
                extractionState = extractionState,
                categories = categories,
                currencyCode = currencyCode,
                onConfirmSave = onConfirmSave
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptLensOcrModal(
    extractionState: AiExtractionState,
    categories: List<CategoryEntity>,
    currencyCode: String,
    onAnalyzeBitmap: (Bitmap, Int?) -> Unit,
    onConfirmSave: (
        merchant: String,
        amount: Double,
        dateString: String,
        categoryName: String,
        note: String,
        tagsCsv: String,
        taxAmount: Double,
        lineItems: String,
        isSubscription: Boolean,
        sentimentNote: String,
        sourceType: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sampleIndex by remember { mutableIntStateOf(0) }

    // 1. Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                if (bmp != null) {
                    previewBitmap = bmp
                    onAnalyzeBitmap(bmp, null)
                }
            } catch (_: Exception) {}
        }
    }

    // 2. Camera Capture Preview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            previewBitmap = bmp
            onAnalyzeBitmap(bmp, null)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LuminaIndigoContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = LuminaIndigoLight
                    )
                }
                Column {
                    Text(
                        text = "Receipt Lens — Gemini Vision OCR",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Extracts vendor, date, line items, GST split & total",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Capture Options: Camera, Photo Picker, Sample GST Receipt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("receipt_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("receipt_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = {
                        val currentPreset = sampleIndex++
                        val bmp = GeminiExpenseAiService.createSampleReceiptBitmap(currentPreset)
                        previewBitmap = bmp
                        onAnalyzeBitmap(bmp, currentPreset)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuminaEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("receipt_sample_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Sample Receipt",
                        tint = LuminaEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Demo Bill", style = MaterialTheme.typography.labelLarge, color = LuminaEmerald)
                }
            }

            if (previewBitmap != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = "Scanned Receipt Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AiExtractionReviewSection(
                extractionState = extractionState,
                categories = categories,
                currencyCode = currencyCode,
                onConfirmSave = onConfirmSave
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiExtractionReviewSection(
    extractionState: AiExtractionState,
    categories: List<CategoryEntity>,
    currencyCode: String,
    onConfirmSave: (
        merchant: String,
        amount: Double,
        dateString: String,
        categoryName: String,
        note: String,
        tagsCsv: String,
        taxAmount: Double,
        lineItems: String,
        isSubscription: Boolean,
        sentimentNote: String,
        sourceType: String
    ) -> Unit
) {
    when (extractionState) {
        is AiExtractionState.Idle -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Structured JSON fields (Merchant, Amount, Category, Tax Split, Line Items & Tags) will appear here after Gemini extraction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        is AiExtractionState.Loading -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, LuminaIndigo.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = LuminaIndigoLight,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = extractionState.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
        is AiExtractionState.Error -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, LuminaCrimson),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = extractionState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = LuminaCrimson,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        is AiExtractionState.Ready -> {
            val draft: ParsedExpenseDraft = extractionState.draft
            var merchant by remember(draft) { mutableStateOf(draft.merchant) }
            var amountText by remember(draft) {
                mutableStateOf(if (draft.amount > 0) draft.amount.toString() else "")
            }
            var dateStr by remember(draft) { mutableStateOf(draft.date) }
            var selectedCat by remember(draft) { mutableStateOf(draft.category) }
            var note by remember(draft) { mutableStateOf(draft.note) }
            var tagsCsv by remember(draft) { mutableStateOf(draft.tags.joinToString(", ")) }
            var taxText by remember(draft) {
                mutableStateOf(if (draft.taxAmount > 0) draft.taxAmount.toString() else "0")
            }
            var lineItems by remember(draft) { mutableStateOf(draft.lineItemsSummary) }
            var isSub by remember(draft) { mutableStateOf(draft.isSubscription) }

            LaunchedEffect(draft) {
                merchant = draft.merchant
                amountText = if (draft.amount > 0) draft.amount.toString() else ""
                dateStr = draft.date
                selectedCat = draft.category
                note = draft.note
                tagsCsv = draft.tags.joinToString(", ")
                taxText = if (draft.taxAmount > 0) draft.taxAmount.toString() else "0"
                lineItems = draft.lineItemsSummary
                isSub = draft.isSubscription
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, LuminaEmerald.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_extraction_review_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ ${draft.statusNotice}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (draft.aiPowered) LuminaEmerald else LuminaAmber,
                            fontWeight = FontWeight.Bold
                        )
                        if (draft.sentimentNote.isNotBlank()) {
                            Text(
                                text = draft.sentimentNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            label = { Text("Merchant") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount ($currencyCode)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = selectedCat,
                            onValueChange = { selectedCat = it },
                            label = { Text("Category (or Custom)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = dateStr,
                            onValueChange = { dateStr = it },
                            label = { Text("Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick category chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val active = selectedCat.equals(cat.name, ignoreCase = true)
                            Surface(
                                onClick = { selectedCat = cat.name },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) LuminaIndigoContainer else ObsidianCardElevated,
                                border = BorderStroke(
                                    1.dp,
                                    if (active) LuminaIndigo else ObsidianBorder
                                )
                            ) {
                                Text(
                                    text = "${cat.emoji} ${cat.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) TextPrimary else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (lineItems.isNotBlank() || (taxText.toDoubleOrNull() ?: 0.0) > 0.0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = lineItems,
                                onValueChange = { lineItems = it },
                                label = { Text("Extracted Line Items") },
                                singleLine = true,
                                modifier = Modifier.weight(1.4f)
                            )
                            OutlinedTextField(
                                value = taxText,
                                onValueChange = { taxText = it },
                                label = { Text("Tax Split") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Note") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tagsCsv,
                            onValueChange = { tagsCsv = it },
                            label = { Text("Tags (comma sep)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recurring Subscription / Bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Switch(
                            checked = isSub,
                            onCheckedChange = { isSub = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuminaIndigo)
                        )
                    }

                    Button(
                        onClick = {
                            val parsedAmt = amountText.toDoubleOrNull() ?: 0.0
                            val parsedTax = taxText.toDoubleOrNull() ?: 0.0
                            if (parsedAmt > 0.0) {
                                onConfirmSave(
                                    merchant,
                                    parsedAmt,
                                    dateStr,
                                    selectedCat,
                                    note,
                                    tagsCsv,
                                    parsedTax,
                                    lineItems,
                                    isSub,
                                    draft.sentimentNote,
                                    extractionState.sourceType
                                )
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuminaEmerald,
                            contentColor = Color(0xFF022C22)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_save_ai_expense_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save to Lumina Ledger",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
