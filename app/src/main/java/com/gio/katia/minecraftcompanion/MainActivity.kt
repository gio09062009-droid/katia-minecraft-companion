package com.gio.katia.minecraftcompanion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var permissionButton: Button
    private lateinit var showButton: Button
    private lateinit var hideButton: Button

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

        permissionButton = Button(this).apply {
            text = "Configurar ventana flotante"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    refreshState()
                }
            }
        }

        showButton = Button(this).apply {
            text = "Mostrar Katia"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(this@MainActivity, OverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    status.text = "Iniciando la ventana flotante de Katia..."
                } else {
                    status.text = "Primero tenés que conceder el permiso de ventana flotante."
                }
                refreshState()
            }
        }

        hideButton = Button(this).apply {
            text = "Ocultar Katia"
            setOnClickListener {
                val intent = Intent(this@MainActivity, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                }
                startService(intent)
                status.text = "Cerrando la ventana flotante..."
                refreshState()
            }
        }

        layout.addView(title, matchWrap())
        layout.addView(subtitle, matchWrap())
        layout.addView(status, matchWrap())
        layout.addView(permissionButton, matchWrap())
        layout.addView(showButton, matchWrap())
        layout.addView(hideButton, matchWrap())

        setContentView(layout)
        refreshState()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshState()
    }

    private fun refreshState() {
        val permitted = Settings.canDrawOverlays(this)
        permissionButton.isEnabled = !permitted
        permissionButton.text = if (permitted) {
            "Permiso de ventana concedido"
        } else {
            "Configurar ventana flotante"
        }

        if (!permitted) {
            status.text = "Falta el permiso para mostrar a Katia sobre Minecraft."
            showButton.isEnabled = false
            hideButton.isEnabled = false
        } else {
            showButton.isEnabled = !OverlayService.isRunning
            hideButton.isEnabled = OverlayService.isRunning
            if (OverlayService.isRunning) {
                status.text = "Katia está activa. Podés volver a Minecraft."
            } else if (status.text.isNullOrBlank() ||
                status.text.toString().contains("Falta el permiso")) {
                status.text = "Permiso concedido. Tocá Mostrar Katia para abrir la ventana flotante."
            }
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
