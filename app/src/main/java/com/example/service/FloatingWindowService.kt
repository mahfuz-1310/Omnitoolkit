package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.NameGenApplication
import com.example.ui.components.SystemOverlayContent
import com.example.ui.theme.NameGenProTheme
import kotlinx.coroutines.*

open class FloatingWindowService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        @Volatile
        var isRunning: Boolean = false
            private set

        private var activeServiceInstance: FloatingWindowService? = null

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FloatingWindowService::class.java)
                context.stopService(intent)
                val intentOverlay = Intent(context, FloatingOverlayService::class.java)
                context.stopService(intentOverlay)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected lateinit var windowManager: WindowManager
    protected var rootContainer: FrameLayout? = null
    protected var bubbleView: ComposeView? = null
    protected var windowCardView: ComposeView? = null
    protected var layoutParams: WindowManager.LayoutParams? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()

        synchronized(FloatingWindowService::class.java) {
            if (activeServiceInstance != null && activeServiceInstance != this) {
                activeServiceInstance?.cleanupOverlay()
                activeServiceInstance?.stopSelf()
            }
            activeServiceInstance = this
        }
        isRunning = true

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupDualWindowOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        synchronized(FloatingWindowService::class.java) {
            if (rootContainer == null) {
                setupDualWindowOverlay()
            } else {
                rootContainer?.let { view ->
                    layoutParams?.let { params ->
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            try {
                                windowManager.removeView(view)
                                windowManager.addView(view, params)
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun setupDualWindowOverlay() {
        synchronized(FloatingWindowService::class.java) {
            if (rootContainer != null) return

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val app = application as NameGenApplication
            val viewModel = app.sharedViewModel

            val density = resources.displayMetrics.density
            val savedX = viewModel.floatingPosX.value.toInt().coerceAtLeast(0)
            val savedY = viewModel.floatingPosY.value.toInt().coerceAtLeast(0)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = if (savedX > 0) savedX else 80
                y = if (savedY > 0) savedY else 160
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            }
            layoutParams = params

            serviceScope.launch {
                viewModel.isFloatingWindowFocusable.collect { focusable ->
                    updateWindowFocusability(focusable)
                }
            }

            val container = FrameLayout(this).apply {
                setViewTreeLifecycleOwner(this@FloatingWindowService)
                setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)
            }

            // 1. Bubble View (Floating circular icon: size 56.dp x 56.dp)
            val bubble = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@FloatingWindowService)
                setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)
                setContent {
                    val darkModeState by viewModel.darkMode.collectAsStateWithLifecycle()
                    val isDarkTheme = darkModeState ?: false
                    val uiColorLong by viewModel.uiColor.collectAsStateWithLifecycle()
                    val buttonColorLong by viewModel.buttonColor.collectAsStateWithLifecycle()

                    NameGenProTheme(
                        darkTheme = isDarkTheme,
                        uiColor = Color(uiColorLong.toInt()),
                        buttonColor = Color(buttonColorLong.toInt())
                    ) {
                        BubbleContent(
                            onTap = { expandWindow() },
                            onDrag = { dx, dy -> updateOverlayPosition(dx, dy) }
                        )
                    }
                }
                visibility = View.VISIBLE
            }

            // 2. Window Card View (Expanded studio panel layout)
            val windowCard = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@FloatingWindowService)
                setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)
                setContent {
                    val darkModeState by viewModel.darkMode.collectAsStateWithLifecycle()
                    val isDarkTheme = darkModeState ?: false
                    val uiColorLong by viewModel.uiColor.collectAsStateWithLifecycle()
                    val buttonColorLong by viewModel.buttonColor.collectAsStateWithLifecycle()

                    NameGenProTheme(
                        darkTheme = isDarkTheme,
                        uiColor = Color(uiColorLong.toInt()),
                        buttonColor = Color(buttonColorLong.toInt())
                    ) {
                        SystemOverlayContent(
                            viewModel = viewModel,
                            onDragDelta = { dx, dy -> updateOverlayPosition(dx, dy) },
                            onMinimize = { minimizeWindow() },
                            onTogglePanel = { togglePanelOptions() },
                            onLaunchApp = { launchMainActivity() },
                            onClose = { closeOverlay() }
                        )
                    }
                }
                visibility = View.GONE
            }

            val metrics = resources.displayMetrics
            val maxWidth = (metrics.widthPixels * 0.90f).toInt()
            val maxHeight = (metrics.heightPixels * 0.75f).toInt()
            val desiredWidth = (360 * density).toInt()
            val desiredHeight = (540 * density).toInt()
            val windowWidth = desiredWidth.coerceAtMost(maxWidth)
            val windowHeight = desiredHeight.coerceAtMost(maxHeight)

            container.addView(bubble, FrameLayout.LayoutParams(
                (52 * density).toInt(),
                (52 * density).toInt()
            ))

            container.addView(windowCard, FrameLayout.LayoutParams(
                windowWidth,
                windowHeight
            ))

            this.rootContainer = container
            this.bubbleView = bubble
            this.windowCardView = windowCard

            try {
                windowManager.addView(container, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected fun expandWindow() {
        val metrics = resources.displayMetrics
        val density = metrics.density
        val maxWidth = (metrics.widthPixels * 0.90f).toInt()
        val maxHeight = (metrics.heightPixels * 0.75f).toInt()
        val desiredWidth = (360 * density).toInt()
        val desiredHeight = (540 * density).toInt()
        
        bubbleView?.visibility = View.GONE
        windowCardView?.visibility = View.VISIBLE

        layoutParams?.let { params ->
            params.width = desiredWidth.coerceAtMost(maxWidth)
            params.height = desiredHeight.coerceAtMost(maxHeight)
            rootContainer?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected fun minimizeWindow() {
        windowCardView?.visibility = View.GONE
        bubbleView?.visibility = View.VISIBLE

        val density = resources.displayMetrics.density
        layoutParams?.let { params ->
            params.width = (52 * density).toInt()
            params.height = (52 * density).toInt()
            rootContainer?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected fun togglePanelOptions() {
        // Expands/collapses the internal options panel inside the floating overlay without launching the main app
        val metrics = resources.displayMetrics
        val density = metrics.density
        val maxWidth = (metrics.widthPixels * 0.90f).toInt()
        val maxHeight = (metrics.heightPixels * 0.75f).toInt()
        val desiredWidth = (360 * density).toInt()
        val desiredHeight = (540 * density).toInt()

        layoutParams?.let { params ->
            params.width = desiredWidth.coerceAtMost(maxWidth)
            params.height = desiredHeight.coerceAtMost(maxHeight)
            rootContainer?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected fun launchMainActivity() {
        try {
            val intent = Intent(this, com.example.MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun closeOverlay() {
        try {
            val app = application as? NameGenApplication
            app?.sharedViewModel?.setFloatingMode(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cleanupOverlay()
        stopSelf()
    }

    private fun updateOverlayPosition(dx: Float, dy: Float) {
        val view = rootContainer ?: return
        val params = layoutParams ?: return

        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val viewW = view.width.takeIf { it > 0 } ?: 200
        val viewH = view.height.takeIf { it > 0 } ?: 200

        val maxX = (screenWidth - viewW).coerceAtLeast(0)
        val maxY = (screenHeight - viewH).coerceAtLeast(0)

        params.x = (params.x + dx.toInt()).coerceIn(0, maxX)
        params.y = (params.y + dy.toInt()).coerceIn(0, maxY)

        try {
            windowManager.updateViewLayout(view, params)
            val app = application as? NameGenApplication
            app?.sharedViewModel?.setFloatingPosition(params.x.toFloat(), params.y.toFloat())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWindowFocusability(focusable: Boolean) {
        val view = rootContainer ?: return
        val params = layoutParams ?: return

        if (focusable) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanupOverlay() {
        rootContainer?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            rootContainer = null
            bubbleView = null
            windowCardView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        cleanupOverlay()
        if (activeServiceInstance == this) {
            activeServiceInstance = null
            isRunning = false
        }
    }
}

@Composable
private fun BubbleContent(
    onTap: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap.asImageBitmap()
            } else {
                val bmp = android.graphics.Bitmap.createBitmap(
                    if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128,
                    if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    Surface(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable { onTap() },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (appIconBitmap != null) {
                Image(
                    bitmap = appIconBitmap,
                    contentDescription = "Floating Studio Icon",
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Floating Studio Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
