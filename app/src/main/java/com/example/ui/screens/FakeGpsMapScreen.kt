package com.example.ui.screens

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.service.MockLocationService
import com.example.utils.FakeGpsManager
import com.example.utils.LocationPreset
import com.example.utils.MapTileManager
import com.example.utils.SoundEffectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import java.io.File
import java.util.Locale

/**
 * Map style choices available in the Map Type Switcher bottom sheet.
 */
enum class AppMapType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String,
    val previewBg: Color
) {
    DEFAULT(
        title = "Default",
        subtitle = "Standard vector street map",
        icon = Icons.Default.Map,
        badge = "Vector",
        previewBg = Color(0xFFE2E8F0)
    ),
    SATELLITE(
        title = "Satellite Hybrid",
        subtitle = "High-res aerial satellite imagery",
        icon = Icons.Default.Satellite,
        badge = "Aerial",
        previewBg = Color(0xFF1E293B)
    ),
    TERRAIN(
        title = "Terrain",
        subtitle = "Topographical elevations & contours",
        icon = Icons.Default.Terrain,
        badge = "Topo",
        previewBg = Color(0xFFD1FAE5)
    ),
    DARK(
        title = "Dark Cyber",
        subtitle = "High contrast dark night mode",
        icon = Icons.Default.DarkMode,
        badge = "Night",
        previewBg = Color(0xFF0F172A)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeGpsMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Initialize Fake GPS manager and osmdroid config with high-performance cache and custom User-Agent
    LaunchedEffect(Unit) {
        FakeGpsManager.init(context)
        MapTileManager.configureOsmdroid(context)
    }

    val isMockActive by FakeGpsManager.isMockingActive.collectAsStateWithLifecycle()
    val savedLat by FakeGpsManager.currentLat.collectAsStateWithLifecycle()
    val savedLng by FakeGpsManager.currentLng.collectAsStateWithLifecycle()
    val mockUpdateCount by FakeGpsManager.updateCounter.collectAsStateWithLifecycle()
    val mockError by FakeGpsManager.lastError.collectAsStateWithLifecycle()

    var centerLat by remember { mutableStateOf(savedLat) }
    var centerLng by remember { mutableStateOf(savedLng) }
    var locationName by remember { mutableStateOf(FakeGpsManager.getSavedPresetName(context)) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showPresetDrawer by remember { mutableStateOf(true) }
    var showFineTuneControls by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showMapTypeSheet by remember { mutableStateOf(false) }
    var currentMapType by remember { mutableStateOf(AppMapType.DEFAULT) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Helper to get matching ITileSource for the selected Map Type
    fun getTileSourceForType(type: AppMapType): ITileSource {
        return when (type) {
            AppMapType.DEFAULT -> MapTileManager.StandardOsmTileSource
            AppMapType.SATELLITE -> MapTileManager.GoogleHybridSatelliteTileSource
            AppMapType.TERRAIN -> MapTileManager.OpenTopoTileSource
            AppMapType.DARK -> MapTileManager.CartoDarkTileSource
        }
    }

    // Update tile source when user selects a new Map Type
    fun applyMapType(type: AppMapType) {
        SoundEffectManager.playClick()
        currentMapType = type
        mapViewRef?.let { map ->
            val tileSource = getTileSourceForType(type)
            map.setTileSource(tileSource)
            map.invalidate()
        }
        showMapTypeSheet = false
    }

    // Pulsing animation for active broadcasting state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Geocoding helper
    fun reverseGeocode(lat: Double, lng: Double) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val addr = addresses[0]
                                val title = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: addr.countryName ?: "Custom Point"
                                val sub = addr.countryName ?: ""
                                locationName = if (sub.isNotEmpty() && !title.contains(sub)) "$title, $sub" else title
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val title = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: addr.countryName ?: "Custom Point"
                            val sub = addr.countryName ?: ""
                            withContext(Dispatchers.Main) {
                                locationName = if (sub.isNotEmpty() && !title.contains(sub)) "$title, $sub" else title
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors in geocoding
            }
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        isSearching = true
        focusManager.clearFocus()
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // 1. Check presets first
                val match = FakeGpsManager.popularPresets.find {
                    it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true)
                }
                if (match != null) {
                    withContext(Dispatchers.Main) {
                        centerLat = match.latitude
                        centerLng = match.longitude
                        locationName = "${match.name}, ${match.country}"
                        mapViewRef?.controller?.animateTo(GeoPoint(match.latitude, match.longitude), 16.0, 1000L)
                        isSearching = false
                    }
                    return@launch
                }

                // 2. Geocoder search
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocationName(query, 1)
                    if (!results.isNullOrEmpty()) {
                        val addr = results[0]
                        withContext(Dispatchers.Main) {
                            centerLat = addr.latitude
                            centerLng = addr.longitude
                            val title = addr.locality ?: addr.featureName ?: query
                            val country = addr.countryName ?: ""
                            locationName = if (country.isNotEmpty()) "$title, $country" else title
                            mapViewRef?.controller?.animateTo(GeoPoint(addr.latitude, addr.longitude), 16.0, 1000L)
                            isSearching = false
                        }
                        return@launch
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Location not found for: '$query'", Toast.LENGTH_SHORT).show()
                    isSearching = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Search error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    isSearching = false
                }
            }
        }
    }

    fun moveToPreset(preset: LocationPreset) {
        SoundEffectManager.playClick()
        centerLat = preset.latitude
        centerLng = preset.longitude
        locationName = "${preset.name}, ${preset.country}"
        mapViewRef?.controller?.animateTo(GeoPoint(preset.latitude, preset.longitude), 16.0, 800L)
        FakeGpsManager.saveCoordinates(context, preset.latitude, preset.longitude, preset.name)
        if (isMockActive) {
            MockLocationService.updateLocation(context, preset.latitude, preset.longitude, preset.name)
        }
    }

    fun nudge(deltaLat: Double, deltaLng: Double) {
        SoundEffectManager.playClick()
        val newLat = (centerLat + deltaLat).coerceIn(-90.0, 90.0)
        val newLng = (centerLng + deltaLng).coerceIn(-180.0, 180.0)
        centerLat = newLat
        centerLng = newLng
        mapViewRef?.controller?.animateTo(GeoPoint(newLat, newLng))
        if (isMockActive) {
            FakeGpsManager.saveCoordinates(context, newLat, newLng, locationName)
            MockLocationService.updateLocation(context, newLat, newLng, locationName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fake GPS Map Picker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (isMockActive) "Live OS Injection: Active ($mockUpdateCount fixes)" else "Drag map to target coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMockActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Mock Setup Help")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. FULL SCREEN HIGH-DEFINITION OSMDROID MAP VIEW
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(getTileSourceForType(currentMapType))
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(centerLat, centerLng))
                        minZoomLevel = 1.0
                        maxZoomLevel = 20.0

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                val center = mapCenter
                                if (center != null) {
                                    centerLat = center.latitude
                                    centerLng = center.longitude
                                    reverseGeocode(center.latitude, center.longitude)
                                }
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                val center = mapCenter
                                if (center != null) {
                                    centerLat = center.latitude
                                    centerLng = center.longitude
                                }
                                return true
                            }
                        })
                        mapViewRef = this
                    }
                },
                update = { view ->
                    mapViewRef = view
                }
            )

            // 2. CENTER PIN MARKER OVERLAY (FIXED AT SCREEN CENTER)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = (-24).dp)
                ) {
                    // Marker speech bubble
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMockActive) Color(0xFF065F46) else MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.5.dp, if (isMockActive) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isMockActive) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                            )
                            Text(
                                text = locationName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMockActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pin Icon with pulsing glow when active
                    Box(contentAlignment = Alignment.Center) {
                        if (isMockActive) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(pulseScale)
                                    .background(Color(0x3310B981), CircleShape)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Center Target Marker",
                            tint = if (isMockActive) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Shadow dot on ground
                    Box(
                        modifier = Modifier
                            .size(10.dp, 4.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    )
                }
            }

            // 3. TOP FLOATING SEARCH & COORDINATES BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .zIndex(20f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search field
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search city, address, or landmark...", fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchLocation(searchQuery) })
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { searchLocation(searchQuery) }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Preset Chips Row
                AnimatedVisibility(visible = showPresetDrawer) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FakeGpsManager.popularPresets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (centerLat == preset.latitude && centerLng == preset.longitude)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                shadowElevation = 4.dp,
                                modifier = Modifier.clickable { moveToPreset(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (centerLat == preset.latitude && centerLng == preset.longitude)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = preset.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (centerLat == preset.latitude && centerLng == preset.longitude)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. MAP CONTROL TOOLS (RIGHT SIDE FLOATING PANEL - INCLUDING MAP TYPE SWITCHER)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .zIndex(20f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // MAP TYPE SWITCHER BUTTON (LAYERS ICON)
                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        showMapTypeSheet = true
                    },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Select Map Type (Default / Satellite / Terrain)",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Zoom In
                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        mapViewRef?.controller?.zoomIn()
                    },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.primary)
                }

                // Zoom Out
                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        mapViewRef?.controller?.zoomOut()
                    },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.primary)
                }

                // D-Pad / Fine Tune Toggle
                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        showFineTuneControls = !showFineTuneControls
                    },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    containerColor = if (showFineTuneControls) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "Fine Tune D-Pad", tint = MaterialTheme.colorScheme.primary)
                }

                // Recenter / Re-anchor
                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        mapViewRef?.controller?.animateTo(GeoPoint(savedLat, savedLng), 16.0, 600L)
                    },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // 5. D-PAD / FINE TUNING CONTROLLER (OPTIONAL OVERLAY)
            AnimatedVisibility(
                visible = showFineTuneControls,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 120.dp)
                    .zIndex(25f)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Fine Nudge (~50m)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { nudge(0.0005, 0.0) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "North", tint = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { nudge(0.0, -0.0005) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "West", tint = MaterialTheme.colorScheme.primary)
                            }
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { nudge(0.0, 0.0005) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "East", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { nudge(-0.0005, 0.0) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "South", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 6. BOTTOM COORDINATES CARD (STATUS & EXACT NUMERICAL VALUES)
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(15f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (isMockActive) Color(0xFF10B981) else Color(0xFF6B7280), CircleShape)
                                )
                                Text(
                                    text = if (isMockActive) "SPOOFING ACTIVE" else "READY TO BROADCAST",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isMockActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lat: %.5f  |  Lng: %.5f".format(centerLat, centerLng),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Developer options quick link
                        TextButton(
                            onClick = { FakeGpsManager.openDeveloperSettings(context) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dev Settings", fontSize = 11.sp)
                        }
                    }

                    if (mockError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = mockError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 7. FLOATING ACTION BUTTON (PLAY / STOP TOGGLE AT BOTTOM RIGHT)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp)
                    .zIndex(30f)
            ) {
                // Pulsing outer ring when broadcasting
                if (isMockActive) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .align(Alignment.Center)
                            .scale(pulseScale)
                            .background(Color(0x3310B981), CircleShape)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        if (!isMockActive) {
                            FakeGpsManager.saveCoordinates(context, centerLat, centerLng, locationName)
                            val started = FakeGpsManager.startMockLocation(context, centerLat, centerLng)
                            if (started) {
                                MockLocationService.start(context, centerLat, centerLng, locationName)
                                Toast.makeText(context, "Spoofing active at ($centerLat, $centerLng)!", Toast.LENGTH_SHORT).show()
                            } else {
                                showHelpDialog = true
                            }
                        } else {
                            MockLocationService.stop(context)
                            FakeGpsManager.stopMockLocation(context)
                            Toast.makeText(context, "Mock Location Stopped", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    containerColor = if (isMockActive) Color(0xFFEF4444) else Color(0xFF10B981),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp, pressedElevation = 14.dp)
                ) {
                    AnimatedContent(
                        targetState = isMockActive,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) + scaleIn() togetherWith
                                    fadeOut(animationSpec = tween(220)) + scaleOut()
                        },
                        label = "fabIcon"
                    ) { active ->
                        if (active) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Spoofing",
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Spoofing",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 8. MAP TYPE SELECTOR BOTTOM SHEET (GOOGLE MAPS STYLE)
    if (showMapTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMapTypeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Map Type & Layers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showMapTypeSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Select map visualization style and tile renderer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Map Type Option Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppMapType.values().forEach { mapType ->
                        val isSelected = currentMapType == mapType
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                2.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { applyMapType(mapType) }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Thumbnail / Icon Container
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else mapType.previewBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = mapType.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = mapType.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = mapType.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feature details info card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Tiles are automatically cached locally for offline and low-bandwidth use.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 9. SETUP & PERMISSIONS HELP DIALOG
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("How to Enable Mock Location", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "To broadcast mock coordinates across Google Maps, WhatsApp, and the entire Android OS:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("1. Open Developer Options on your device.")
                    Text("2. Locate 'Select mock location app'.")
                    Text("3. Choose '${context.applicationInfo.loadLabel(context.packageManager)}'.")
                    Text("4. Return here and tap the Green Play button!")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHelpDialog = false
                        FakeGpsManager.openDeveloperSettings(context)
                    }
                ) {
                    Text("Open Developer Options")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
