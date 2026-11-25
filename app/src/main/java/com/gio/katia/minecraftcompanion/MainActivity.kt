package com.gio.katia.minecraftcompanion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var overlayButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val background = 0xFF100D1C.toInt()
        val purple = 0xFF9B72F2.toInt()
        val white = 0xFFF7F2FF.toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 40, 32, 40)
            setBackgroundColor(background)
        }

        val title = TextView(this).apply {
            text = "KATIA"
            textSize = 34f
            setTextColor(purple)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Minecraft Companion"
            textSize = 18f
            setTextColor(white)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            textSize = 15f
            setTextColor(0xFFD5CBE8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 24)
        }

        overlayButton = Button(this).apply {
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    refreshOverlayPermissionState()
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            }
        }

        layout.addView(title, matchWrap())
        layout.addView(subtitle, matchWrap())
        layout.addView(status, matchWrap())
        layout.addView(overlayButton, matchWrap())

        setContentView(layout)
        refreshOverlayPermissionState()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized && ::overlayButton.isInitialized) {
            refreshOverlayPermissionState()
        }
    }

    private fun refreshOverlayPermissionState() {
        val permissionGranted = Settings.canDrawOverlays(this)

        if (permissionGranted) {
            status.text = "Permiso concedido. Katia ya puede usar la ventana flotante cuando esa función esté implementada."
            overlayButton.text = "Permiso configurado"
            overlayButton.isEnabled = false
        } else {
            status.text = "Concedé el permiso para permitir que Katia pueda mostrarse sobre Minecraft."
            overlayButton.text = "Configurar ventana flotante"
            overlayButton.isEnabled = true
        }
    }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 12
        }
}
