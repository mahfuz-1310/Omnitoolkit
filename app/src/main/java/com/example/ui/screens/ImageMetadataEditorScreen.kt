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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.utils.ExifEditorUtils
import com.example.utils.ExifMetadata
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

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var metadata by remember { mutableStateOf<ExifMetadata?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var removeAllMetadata by remember { mutableStateOf(false) }
    var removeLocationData by remember { mutableStateOf(false) }
    var removeDeviceInfo by remember { mutableStateOf(false) }
    var keepQuality by remember { mutableStateOf(true) }
    var makeInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var dateTimeInput by remember { mutableStateOf("") }

    var showPreviewDialog by remember { mutableStateOf(false) }
    var savedResultUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            savedResultUri = null
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                val meta = ExifEditorUtils.readMetadata(context, uri)
                val bmp = try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    metadata = meta
                    bitmap = bmp
                    makeInput = meta.make ?: ""
                    modelInput = meta.model ?: ""
                    dateTimeInput = meta.dateTimeOriginal ?: ""
                    removeAllMetadata = false
                    removeLocationData = false
                    removeDeviceInfo = false
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
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
                                removeAllMetadata = false
                                removeLocationData = false
                                removeDeviceInfo = false
                                Toast.makeText(context, "Reset to original metadata", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = { showPreviewDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val resultUri = ExifEditorUtils.saveEditedImage(
                                        context = context,
                                        sourceUri = selectedImageUri!!,
                                        removeAllMetadata = removeAllMetadata,
                                        removeLocation = removeLocationData,
                                        removeDevice = removeDeviceInfo,
                                        newDateTime = dateTimeInput,
                                        newMake = makeInput,
                                        newModel = modelInput
                                    )
                                    withContext(Dispatchers.Main) {
                                        if (resultUri != null) {
                                            savedResultUri = resultUri
                                            Toast.makeText(context, "Saved successfully as edited copy!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save edited image.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save As")
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                    // 2) Selected Image Preview Card
                    SelectedImagePreviewCard(
                        bitmap = bitmap,
                        metadata = metadata,
                        imageUri = selectedImageUri!!,
                        onChangeImageClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // 3) Metadata Actions Card (Keep Quality)
                    MetadataActionCard(
                        keepQuality = keepQuality,
                        onKeepQualityChanged = { keepQuality = it }
                    )

                    // 4) Date & Time Editor Card
                    DateTimeEditorCard(
                        dateTimeInput = dateTimeInput,
                        onDateTimeChanged = { dateTimeInput = it },
                        enabled = !removeAllMetadata,
                        onCurrentTimeClick = {
                            val currentTime = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).format(Date())
                            dateTimeInput = currentTime
                        }
                    )

                    // 5) Device Info Editor Card
                    DeviceInfoEditorCard(
                        makeInput = makeInput,
                        onMakeChanged = { makeInput = it },
                        modelInput = modelInput,
                        onModelChanged = { modelInput = it },
                        enabled = !removeAllMetadata && !removeDeviceInfo
                    )

                    // 6) Remove Location Data card
                    RemoveLocationDataCard(
                        removeLocation = removeLocationData,
                        onRemoveLocationChanged = { removeLocationData = it },
                        enabled = !removeAllMetadata
                    )

                    // 7) Remove Device Info card
                    RemoveDeviceInfoCard(
                        removeDevice = removeDeviceInfo,
                        onRemoveDeviceChanged = { removeDeviceInfo = it },
                        enabled = !removeAllMetadata
                    )

                    // 8) Remove All Metadata card
                    RemoveAllMetadataCard(
                        removeAll = removeAllMetadata,
                        onRemoveAllChanged = { removeAllMetadata = it }
                    )

                    if (savedResultUri != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Edited Image Saved Successfully!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Saved at: ${savedResultUri?.lastPathSegment ?: "Internal Storage"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for sticky bar
                }
            }
        }
    }

    if (showPreviewDialog) {
        PreviewChangesDialog(
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    modifier = Modifier.size(28.dp)
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
                    text = "Remove EXIF data, edit date, and update device info securely.",
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
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
                text = "Select an image from your gallery to view and edit its EXIF metadata, camera make, model, and capture timestamp.",
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

@Composable
fun SelectedImagePreviewCard(
    bitmap: Bitmap?,
    metadata: ExifMetadata?,
    imageUri: Uri,
    onChangeImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface),
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
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = imageUri.lastPathSegment ?: "Selected Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dimensions: ${metadata?.width ?: "?"} × ${metadata?.height ?: "?"} px",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!metadata?.make.isNullOrBlank() || !metadata?.model.isNullOrBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text("EXIF Present", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        } else {
                            AssistChip(
                                onClick = {},
                                label = { Text("No EXIF", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
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
fun MetadataActionCard(
    keepQuality: Boolean,
    onKeepQualityChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Export Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Keep Image Quality Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Preserve Original Quality", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Save edited copy without compression loss",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = keepQuality,
                    onCheckedChange = onKeepQualityChanged
                )
            }
        }
    }
}

@Composable
fun RemoveLocationDataCard(
    removeLocation: Boolean,
    onRemoveLocationChanged: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strip GPS / Geo Tags",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Removes real location information from photo metadata",
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

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Visible content in the image will not change",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strip Make / Model",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Removes camera or device information stored in metadata",
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strips EXIF, GPS, device info, and other embedded metadata",
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
fun DateTimeEditorCard(
    dateTimeInput: String,
    onDateTimeChanged: (String) -> Unit,
    enabled: Boolean,
    onCurrentTimeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    TextButton(onClick = onCurrentTimeClick) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use Now")
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
fun PreviewChangesDialog(
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
                Text("Metadata Changes Preview")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when {
                        removeAllMetadata -> "• All metadata will be stripped"
                        removeLocationData && removeDeviceInfo -> "• GPS location data will be removed\n• Device info will be removed"
                        removeLocationData -> "• GPS location data will be removed"
                        removeDeviceInfo -> "• Device info will be removed"
                        else -> "• Custom EXIF updates applied"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                if (removeAllMetadata) {
                    Text("• All EXIF metadata, GPS geotags, make, model, and timestamps will be completely wiped.")
                } else {
                    if (removeLocationData) {
                        Text("• GPS coordinates and location tags will be removed.")
                    }
                    if (removeDeviceInfo) {
                        Text("• Device make and model tags will be removed.")
                    } else {
                        Text("• Make: ${metadata?.make ?: "None"} -> ${if (makeInput.isBlank()) "(Cleared)" else makeInput}")
                        Text("• Model: ${metadata?.model ?: "None"} -> ${if (modelInput.isBlank()) "(Cleared)" else modelInput}")
                    }
                    Text("• Date/Time: ${metadata?.dateTimeOriginal ?: "None"} -> ${if (dateTimeInput.isBlank()) "(Cleared)" else dateTimeInput}")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🛡️ Non-Destructive Guarantee", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Original image will stay unchanged. Changes are saved to a new file copy.",
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
        shape = RoundedCornerShape(24.dp)
    )
}
