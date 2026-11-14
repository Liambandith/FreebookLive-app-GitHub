package com.example.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Lobby
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class FloatingLobbyService : Service() {

    private var windowManager: WindowManager? = null
    private var rootView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var worldName = "Alex's Survival Kingdom"
    private var gamePlatform = "Minecraft"
    private var hostUsername = "Alex_Overlord"
    private var inviteCode = "FBL-1001"
    private var playersText = "3/8"
    private var robloxPlaceId = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Update lobby details if passed
        intent?.let {
            worldName = it.getStringExtra(EXTRA_WORLD_NAME) ?: worldName
            gamePlatform = it.getStringExtra(EXTRA_PLATFORM) ?: gamePlatform
            hostUsername = it.getStringExtra(EXTRA_HOST) ?: hostUsername
            inviteCode = it.getStringExtra(EXTRA_INVITE_CODE) ?: inviteCode
            playersText = it.getStringExtra(EXTRA_PLAYERS) ?: playersText
            robloxPlaceId = it.getStringExtra(EXTRA_ROBLOX_PLACE_ID) ?: robloxPlaceId
        }

        val notification = buildNotification()
        val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, fgsType)

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        _isRunning.value = true
        renderOverlay()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        removeOverlayView()
    }

    private fun removeOverlayView() {
        rootView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
            }
        }
        rootView = null
    }

    private fun renderOverlay() {
        removeOverlayView()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val existingX = layoutParams?.x ?: (screenWidth - dp(72))
        val existingY = layoutParams?.y ?: dp(180)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = existingX.coerceIn(0, (screenWidth - dp(64)).coerceAtLeast(0))
            y = existingY.coerceAtLeast(dp(48))
        }

        rootView = if (isExpanded) buildExpandedHudView() else buildCollapsedBubbleView()

        try {
            windowManager?.addView(rootView, layoutParams)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildCollapsedBubbleView(): View {
        val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
        val accentColor = if (isRoblox) Color.parseColor("#00A2FF") else Color.parseColor("#10B981")

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val size = dp(58)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0F172A"))
                setStroke(dp(3), accentColor)
            }
            elevation = dp(10).toFloat()
        }

        val title = TextView(this).apply {
            text = if (isRoblox) "RBX" else "MC"
            setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val badge = TextView(this).apply {
            text = playersText
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        bubble.addView(title)
        bubble.addView(badge)

        attachDragAndTapListener(bubble) {
            isExpanded = true
            renderOverlay()
        }

        return bubble
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildExpandedHudView(): View {
        val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
        val accentColor = if (isRoblox) Color.parseColor("#00A2FF") else Color.parseColor("#10B981")
        val glowColor = if (isRoblox) Color.parseColor("#7DD3FC") else Color.parseColor("#6EE7B7")

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LinearLayout.LayoutParams(dp(265), LinearLayout.LayoutParams.WRAP_CONTENT)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(Color.parseColor("#F00A0E17"))
                setStroke(dp(2), accentColor)
            }
            elevation = dp(14).toFloat()
        }

        // Top Drag Handle / Header
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val platformPill = TextView(this).apply {
            text = " ${gamePlatform.uppercase()} • $playersText "
            setTextColor(if (isRoblox) Color.WHITE else Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(6), dp(2), dp(6), dp(2))
            background = GradientDrawable().apply {
                cornerRadius = dp(6).toFloat()
                setColor(accentColor)
            }
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        val minBtn = TextView(this).apply {
            text = "–"
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), 0, dp(10), 0)
            setOnClickListener {
                isExpanded = false
                renderOverlay()
            }
        }

        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#EF4444"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), 0, dp(2), 0)
            setOnClickListener { stopSelf() }
        }

        headerRow.addView(platformPill)
        headerRow.addView(spacer)
        headerRow.addView(minBtn)
        headerRow.addView(closeBtn)

        val worldTitle = TextView(this).apply {
            text = worldName
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(2))
        }

        val hostInfo = TextView(this).apply {
            text = if (isRoblox) "Host: @$hostUsername • Code: $inviteCode" else "Host: $hostUsername • Code: $inviteCode"
            setTextColor(glowColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        }

        val hintText = TextView(this).apply {
            text = if (isRoblox) {
                "Tap Open Roblox to jump into Place ID ${robloxPlaceId.ifBlank { inviteCode }}"
            } else {
                "Open Minecraft > Play > Worlds & tap '$worldName'"
            }
            setTextColor(Color.parseColor("#CBD5E1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(0, dp(6), 0, dp(10))
        }

        // Action Buttons Row
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val launchGameBtn = TextView(this).apply {
            text = if (isRoblox) "Open Roblox" else "Open Minecraft"
            setTextColor(if (isRoblox) Color.WHITE else Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(6)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(accentColor)
            }
            setOnClickListener { openGameFromOverlay() }
        }

        val appBtn = TextView(this).apply {
            text = "Lobby App"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dp(1), Color.parseColor("#475569"))
            }
            setOnClickListener {
                val appIntent = Intent(this@FloatingLobbyService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(appIntent)
                isExpanded = false
                renderOverlay()
            }
        }

        btnRow.addView(launchGameBtn)
        btnRow.addView(appBtn)

        container.addView(headerRow)
        container.addView(worldTitle)
        container.addView(hostInfo)
        container.addView(hintText)
        container.addView(btnRow)

        attachDragAndTapListener(headerRow, null)

        return container
    }

    private fun openGameFromOverlay() {
        val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
        try {
            if (isRoblox) {
                val placeId = robloxPlaceId.ifBlank { "4924922222" }
                val uri = Uri.parse("roblox://experiences/start?placeId=$placeId")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } else {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("minecraft://")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
        } catch (_: Exception) {
            val appIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(appIntent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachDragAndTapListener(targetView: View, onTap: (() -> Unit)?) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        val screenWidth = resources.displayMetrics.widthPixels

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        targetView.setOnTouchListener { _, event ->
            val params = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        isDragging = true
                    }
                    params.x = initialX + dx
                    params.y = (initialY + dy).coerceAtLeast(dp(24))
                    try {
                        windowManager?.updateViewLayout(rootView, params)
                    } catch (_: Exception) {
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging && onTap != null) {
                        onTap()
                    } else if (!isExpanded) {
                        // Snap collapsed bubble to nearest horizontal screen edge
                        val snapX = if (params.x + dp(29) < screenWidth / 2) dp(8) else screenWidth - dp(66)
                        params.x = snapX.coerceAtLeast(0)
                        try {
                            windowManager?.updateViewLayout(rootView, params)
                        } catch (_: Exception) {
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Multiplayer HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows while FreebookLive floating bubble is active over games"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingLobbyService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FreebookLive Floating HUD")
            .setContentText("$gamePlatform • $worldName ($inviteCode)")
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, "Close Floating", stopIntent)
            .build()
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        const val CHANNEL_ID = "freebooklive_floating_hud"
        const val NOTIFICATION_ID = 7401
        const val ACTION_STOP = "com.example.overlay.ACTION_STOP"

        const val EXTRA_WORLD_NAME = "extra_world_name"
        const val EXTRA_PLATFORM = "extra_platform"
        const val EXTRA_HOST = "extra_host"
        const val EXTRA_INVITE_CODE = "extra_invite_code"
        const val EXTRA_PLAYERS = "extra_players"
        const val EXTRA_ROBLOX_PLACE_ID = "extra_roblox_place_id"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context, lobby: Lobby) {
            val intent = Intent(context, FloatingLobbyService::class.java).apply {
                putExtra(EXTRA_WORLD_NAME, lobby.name)
                putExtra(EXTRA_PLATFORM, lobby.gamePlatform)
                putExtra(EXTRA_HOST, lobby.hostUsername)
                putExtra(EXTRA_INVITE_CODE, lobby.inviteCode)
                putExtra(EXTRA_PLAYERS, "${lobby.currentPlayers}/${lobby.maxPlayers}")
                putExtra(EXTRA_ROBLOX_PLACE_ID, lobby.robloxPlaceId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingLobbyService::class.java))
        }
    }
}
