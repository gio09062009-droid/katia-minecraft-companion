package com.gio.katia.minecraftcompanion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {

    companion object {
        const val ACTION_STOP = "com.gio.katia.minecraftcompanion.STOP_OVERLAY"
        @Volatile var isRunning: Boolean = false
            private set
        private const val CHANNEL_ID = "katia_overlay_channel"
        private const val NOTIFICATION_ID = 41
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isCollapsed = false
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(150)
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopOverlay()
            return START_NOT_STICKY
        }

        startAsForegroundService()
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (overlayView == null) {
            showOverlay()
        }
        return START_STICKY
    }

    private fun startAsForegroundService() {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Katia está activa")
            .setContentText("La ventana flotante está disponible sobre Minecraft.")
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Katia - ventana flotante",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activa la ventana flotante de Katia."
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun showOverlay() {
        overlayView = createExpandedView()
        windowManager.addView(overlayView, layoutParams)
    }

    private fun createExpandedView(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(10), dp(10))
            background = roundedBackground(0xF21A1428.toInt(), 0xFF9B72F2.toInt(), 22)
        }

        val avatar = TextView(this).apply {
            text = "K"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = roundedBackground(0xFF8052D6.toInt(), 0xFFB99AFF.toInt(), 40)
        }
        card.addView(avatar, LinearLayout.LayoutParams(dp(44), dp(44)))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(8), 0)
        }
        val name = TextView(this).apply {
            text = "KATIA"
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "Estoy aquí, explorador."
            textSize = 11f
            setTextColor(0xFFD9C9FF.toInt())
        }
        info.addView(name)
        info.addView(subtitle)
        card.addView(info)

        val minimize = actionButton("−")
        minimize.setOnClickListener { collapseOverlay() }
        card.addView(minimize, buttonParams())

        val close = actionButton("×")
        close.setOnClickListener { stopOverlay() }
        card.addView(close, buttonParams())

        info.setOnTouchListener(dragListener())
        name.setOnTouchListener(dragListener())
        subtitle.setOnTouchListener(dragListener())
        avatar.setOnTouchListener(dragListener())
        return card
    }

    private fun createCollapsedView(): View {
        val orb = TextView(this).apply {
            text = "K"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = roundedBackground(0xFF8052D6.toInt(), 0xFFE0D0FF.toInt(), 48)
            setOnClickListener { expandOverlay() }
        }
        orb.setOnTouchListener(dragListener())
        return orb
    }

    private fun collapseOverlay() {
        isCollapsed = true
        replaceOverlayView(createCollapsedView(), dp(54), dp(54))
    }

    private fun expandOverlay() {
        isCollapsed = false
        replaceOverlayView(createExpandedView(), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun replaceOverlayView(view: View, width: Int, height: Int) {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = view
        layoutParams.width = width
        layoutParams.height = height
        windowManager.addView(view, layoutParams)
    }

    private fun dragListener(): View.OnTouchListener {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = layoutParams.x
                    startY = layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = startX + (event.rawX - touchX).toInt()
                    layoutParams.y = startY + (event.rawY - touchY).toInt()
                    overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
                    true
                }
                else -> false
            }
        }
    }

    private fun actionButton(label: String) = TextView(this).apply {
        text = label
        textSize = 22f
        gravity = Gravity.CENTER
        setTextColor(0xFFFFFFFF.toInt())
        background = roundedBackground(0xFF35264F.toInt(), 0xFF6F55A0.toInt(), 18)
    }

    private fun buttonParams() = LinearLayout.LayoutParams(dp(36), dp(36)).apply {
        leftMargin = dp(5)
    }

    private fun roundedBackground(color: Int, stroke: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun stopOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
