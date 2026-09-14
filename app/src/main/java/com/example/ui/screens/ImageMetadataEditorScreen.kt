package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.utils.ExifEditorUtils
import com.example.utils.ExifMetadata
import com.example.utils.ExportFormat
import com.example.utils.SaveResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageMetadataEditorScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var metadata by remember { mutableStateOf<ExifMetadata?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalFileName by remember { mutableStateOf("") }

    // Export & file settings
    var customFileName by remember { mutableStateOf("") }
    var subFolderName by remember { mutableStateOf("Image Metadata Editor") }
    var selectedFormat by remember { mutableStateOf(ExportFormat.ORIGINAL) }
    var exportQuality by remember { mutableFloatStateOf(95f) }
    var preserveLossless by remember { mutableStateOf(true) }

    // Metadata modifications
    var removeAllMetadata by remember { mutableStateOf(false) }
    var removeLocationData by remember { mutableStateOf(false) }
    var removeDeviceInfo by remember { mutableStateOf(false) }
    var makeInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var dateTimeInput by remember { mutableStateOf("") }

    var showPreviewDialog by remember { mutableStateOf(false) }
    var saveResult by remember { mutableStateOf<SaveResult?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            saveResult = null
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                val origName = ExifEditorUtils.getOriginalFileName(context, uri)
                val meta = ExifEditorUtils.readMetadata(context, uri)
                val bmp = try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    originalFileName = origName
                    val baseName = origName.substringBeforeLast(".")
                    val ext = origName.substringAfterLast(".", "jpg")
                    customFileName = "edited_${baseName}.$ext"
                    metadata = meta
                    bitmap = bmp
                    makeInput = meta.make ?: ""
                    modelInput = meta.model ?: ""
                    dateTimeInput = meta.dateTimeOriginal ?: ""
                    removeAllMetadata = false
                    removeLocationData = false
                    removeDeviceInfo = false
                    preserveLossless = true
                    selectedFormat = ExportFormat.ORIGINAL
                    exportQuality = 95f
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Image Metadata Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (selectedImageUri != null) {
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                makeInput = metadata?.make ?: ""
                                modelInput = metadata?.model ?: ""
                                dateTimeInput = metadata?.dateTimeOriginal ?: ""
                                val baseName = originalFileName.substringBeforeLast(".")
                                val ext = originalFileName.substringAfterLast(".", "jpg")
                                customFileName = "edited_${baseName}.$ext"
                                removeAllMetadata = false
                                removeLocationData = false
                                removeDeviceInfo = false
                                preserveLossless = true
                                selectedFormat = ExportFormat.ORIGINAL
                                exportQuality = 95f
                                Toast.makeText(context, "Reset to original metadata", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = { showPreviewDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }

                        Button(
                            onClick = {
                                if (isSaving) return@Button
                                isSaving = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = ExifEditorUtils.saveEditedImageToGallery(
                                        context = context,
                                        sourceUri = selectedImageUri!!,
                                        customFileName = customFileName,
                                        subFolder = subFolderName,
                                        format = selectedFormat,
                                        quality = exportQuality.toInt(),
                                        keepLossless = preserveLossless,
                                        removeAllMetadata = removeAllMetadata,
                                        removeLocation = removeLocationData,
                                        removeDevice = removeDeviceInfo,
                                        newDateTime = dateTimeInput,
                                        newMake = makeInput,
                                        newModel = modelInput
                                    )
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        if (result != null) {
                                            saveResult = result
                                            snackbarHostState.showSnackbar(
                                                message = "Saved to Gallery! (${result.displayName})",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to save image to Gallery.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save As", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1) Top Hero Section
                HeroSection()

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (selectedImageUri == null) {
                    // Empty State Card
                    EmptyStateCard(onSelectClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    })
                } else {
                    // 2) Selected Image Preview Card with Metadata Chips
                    SelectedImagePreviewCard(
                        bitmap = bitmap,
                        metadata = metadata,
                        fileName = originalFileName,
                        onChangeImageClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // 3) Export Destination & Filename Card
                    ExportDestinationCard(
                        fileName = customFileName,
                        onFileNameChange = { customFileName = it },
                        subFolder = subFolderName,
                        onSubFolderChange = { subFolderName = it }
                    )

                    // 4) Export Format & Quality Card
                    ExportFormatAndQualityCard(
                        selectedFormat = selectedFormat,
                        onFormatSelected = { selectedFormat = it },
                        exportQuality = exportQuality,
                        onQualityChange = { exportQuality = it },
                        preserveLossless = preserveLossless,
                        onPreserveLosslessChange = { preserveLossless = it }
                    )

                    // 5) Date & Time Editor Card
                    DateTimeEditorCard(
                        dateTimeInput = dateTimeInput,
                        onDateTimeChanged = { dateTimeInput = it },
                        enabled = !removeAllMetadata,
                        onCurrentTimeClick = {
                            val currentTime = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).format(Date())
                            dateTimeInput = currentTime
                        },
                        onClearClick = { dateTimeInput = "" }
                    )

                    // 6) Device Info Editor Card
                    DeviceInfoEditorCard(
                        makeInput = makeInput,
                        onMakeChanged = { makeInput = it },
                        modelInput = modelInput,
                        onModelChanged = { modelInput = it },
                        enabled = !removeAllMetadata && !removeDeviceInfo
                    )

                    // 7) Remove Location Data card (GPS Geotags)
                    RemoveLocationDataCard(
                        removeLocation = removeLocationData,
                        onRemoveLocationChanged = { removeLocationData = it },
                        hasGps = metadata?.hasGps == true,
                        enabled = !removeAllMetadata
                    )

                    // 8) Remove Device Info card
                    RemoveDeviceInfoCard(
                        removeDevice = removeDeviceInfo,
                        onRemoveDeviceChanged = { removeDeviceInfo = it },
                        enabled = !removeAllMetadata
                    )

                    // 9) Remove All Metadata card
                    RemoveAllMetadataCard(
                        removeAll = removeAllMetadata,
                        onRemoveAllChanged = { removeAllMetadata = it }
                    )

                    // 10) Saved Result Success Card
                    if (saveResult != null) {
                        SavedSuccessCard(
                            saveResult = saveResult!!,
                            onOpenGallery = { ExifEditorUtils.openInGallery(context, saveResult!!.uri) },
                            onShare = { ExifEditorUtils.shareImage(context, saveResult!!.uri, saveResult!!.mimeType) }
                        )
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showPreviewDialog) {
        PreviewChangesDialog(
            originalFileName = originalFileName,
            customFileName = customFileName,
            subFolder = subFolderName,
            format = selectedFormat,
            quality = exportQuality.toInt(),
            removeAllMetadata = removeAllMetadata,
            removeLocationData = removeLocationData,
            removeDeviceInfo = removeDeviceInfo,
            makeInput = makeInput,
            modelInput = modelInput,
            dateTimeInput = dateTimeInput,
            metadata = metadata,
            onDismiss = { showPreviewDialog = false }
        )
    }
}

@Composable
fun HeroSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Image Metadata Editor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Clean EXIF tags, edit date/device, and save directly to Gallery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(onSelectClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "No Image Selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose an image from your device to view its EXIF metadata, edit camera details, strip GPS coordinates, and export straight to your Gallery.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSelectClick,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Image")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedImagePreviewCard(
    bitmap: Bitmap?,
    metadata: ExifMetadata?,
    fileName: String,
    onChangeImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected Image Thumbnail",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName.ifBlank { "Selected Image" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dimensions: ${metadata?.width ?: "?"} × ${metadata?.height ?: "?"} px",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!metadata?.dateTimeOriginal.isNullOrBlank()) {
                        Text(
                            text = "Date: ${metadata?.dateTimeOriginal}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Summary Chips Flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // EXIF Status Chip
                val hasExif = !metadata?.make.isNullOrBlank() || !metadata?.model.isNullOrBlank() || !metadata?.dateTimeOriginal.isNullOrBlank()
                AssistChip(
                    onClick = {},
                    label = { Text(if (hasExif) "EXIF Found" else "No EXIF", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            if (hasExif) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (hasExif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // GPS Status Chip
                AssistChip(
                    onClick = {},
                    label = { Text(if (metadata?.hasGps == true) "📍 GPS Geotagged" else "No GPS", style = MaterialTheme.typography.labelSmall) }
                )

                // Device Chip
                if (!metadata?.make.isNullOrBlank() || !metadata?.model.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "${metadata?.make ?: ""} ${metadata?.model ?: ""}".trim(),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            OutlinedButton(
                onClick = onChangeImageClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Change Image")
            }
        }
    }
}

@Composable
fun ExportDestinationCard(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    subFolder: String,
    onSubFolderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Gallery Save Location & Filename",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Public gallery folder location notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save to Public Gallery (Pictures)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pictures/Arw Hyper Toolkit/${subFolder.ifBlank { "Image Metadata Editor" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fileName,
                onValueChange = onFileNameChange,
                label = { Text("Output Filename") },
                placeholder = { Text("e.g. edited_photo.jpg") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) }
            )

            OutlinedTextField(
                value = subFolder,
                onValueChange = onSubFolderChange,
                label = { Text("Album Subfolder") },
                placeholder = { Text("Image Metadata Editor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
            )
        }
    }
}

@Composable
fun ExportFormatAndQualityCard(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    exportQuality: Float,
    onQualityChange: (Float) -> Unit,
    preserveLossless: Boolean,
    onPreserveLosslessChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Export Format & Quality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Export format",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Format Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.values().forEach { fmt ->
                    FilterChip(
                        selected = selectedFormat == fmt,
                        onClick = { onFormatSelected(fmt) },
                        label = {
                            Text(
                                text = when (fmt) {
                                    ExportFormat.ORIGINAL -> "Original"
                                    ExportFormat.JPEG -> "JPEG"
                                    ExportFormat.PNG -> "PNG"
                                    ExportFormat.WEBP -> "WebP"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedFormat == ExportFormat.ORIGINAL || selectedFormat == ExportFormat.JPEG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Preserve Original Quality", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Save without recompression loss",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preserveLossless,
                        onCheckedChange = onPreserveLosslessChange
                    )
                }
            }

            if (!preserveLossless || (selectedFormat != ExportFormat.ORIGINAL && selectedFormat != ExportFormat.PNG)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Image quality",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${exportQuality.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = exportQuality,
                        onValueChange = onQualityChange,
                        valueRange = 10f..100f,
                        steps = 18
                    )
                }
            }
        }
    }
}

@Composable
fun DateTimeEditorCard(
    dateTimeInput: String,
    onDateTimeChanged: (String) -> Unit,
    enabled: Boolean,
    onCurrentTimeClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date & Time Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (enabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onClearClick) {
                            Text("Clear")
                        }
                        TextButton(onClick = onCurrentTimeClick) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Now")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = dateTimeInput,
                onValueChange = onDateTimeChanged,
                label = { Text("Capture Date & Time") },
                placeholder = { Text("YYYY:MM:DD HH:MM:SS") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )

            Text(
                text = "Updates DateTimeOriginal and CreateDate EXIF tags.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeviceInfoEditorCard(
    makeInput: String,
    onMakeChanged: (String) -> Unit,
    modelInput: String,
    onModelChanged: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Device & Camera Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = makeInput,
                onValueChange = onMakeChanged,
                label = { Text("Manufacturer (Make)") },
                placeholder = { Text("e.g. Apple, Google, Samsung") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) }
            )

            OutlinedTextField(
                value = modelInput,
                onValueChange = onModelChanged,
                label = { Text("Device Model") },
                placeholder = { Text("e.g. iPhone 15 Pro, Pixel 8") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
            )

            Text(
                text = "Edit camera manufacturer and model metadata.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RemoveLocationDataCard(
    removeLocation: Boolean,
    onRemoveLocationChanged: (Boolean) -> Unit,
    hasGps: Boolean,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remove Location Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasGps) "📍 GPS geotags detected in image" else "Strip GPS / Geo Tags",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Removes coordinates and location tags from photo metadata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = removeLocation,
                    onCheckedChange = onRemoveLocationChanged,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun RemoveDeviceInfoCard(
    removeDevice: Boolean,
    onRemoveDeviceChanged: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remove Device Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Strip Make / Model",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Removes camera or device info from metadata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = removeDevice,
                    onCheckedChange = onRemoveDeviceChanged,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun RemoveAllMetadataCard(
    removeAll: Boolean,
    onRemoveAllChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remove All Metadata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Strips EXIF, GPS, device info, and timestamps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = removeAll,
                    onCheckedChange = onRemoveAllChanged,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun SavedSuccessCard(
    saveResult: SaveResult,
    onOpenGallery: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved to Gallery!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = saveResult.relativePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenGallery,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Gallery")
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
fun PreviewChangesDialog(
    originalFileName: String,
    customFileName: String,
    subFolder: String,
    format: ExportFormat,
    quality: Int,
    removeAllMetadata: Boolean,
    removeLocationData: Boolean,
    removeDeviceInfo: Boolean,
    makeInput: String,
    modelInput: String,
    dateTimeInput: String,
    metadata: ExifMetadata?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Export & Metadata Preview")
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when {
                        removeAllMetadata -> "• All metadata will be stripped"
                        removeLocationData && removeDeviceInfo -> "• GPS location & Device info will be stripped"
                        removeLocationData -> "• GPS location data will be stripped"
                        removeDeviceInfo -> "• Device make and model will be stripped"
                        else -> "• Custom EXIF metadata changes applied"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "📁 Destination: Pictures/Arw Hyper Toolkit/${subFolder.ifBlank { "Image Metadata Editor" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "📄 Filename: $customFileName",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "🎨 Format: ${format.displayName} (${quality}% Quality)",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (removeAllMetadata) {
                    Text("• All EXIF metadata, GPS geotags, make, model, and timestamps will be completely removed.")
                } else {
                    if (removeLocationData) {
                        Text("• GPS coordinates: (Removed)")
                    } else if (metadata?.hasGps == true) {
                        Text("• GPS coordinates: Preserved (${metadata.latitude}, ${metadata.longitude})")
                    }

                    if (removeDeviceInfo) {
                        Text("• Camera Make/Model: (Removed)")
                    } else {
                        Text("• Make: ${metadata?.make ?: "None"} ➔ ${if (makeInput.isBlank()) "(Cleared)" else makeInput}")
                        Text("• Model: ${metadata?.model ?: "None"} ➔ ${if (modelInput.isBlank()) "(Cleared)" else modelInput}")
                    }
                    Text("• Date/Time: ${metadata?.dateTimeOriginal ?: "None"} ➔ ${if (dateTimeInput.isBlank()) "(Cleared)" else dateTimeInput}")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🛡️ Non-Destructive Guarantee", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Original image will stay unchanged. Changes are saved to a new file in your public Gallery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
