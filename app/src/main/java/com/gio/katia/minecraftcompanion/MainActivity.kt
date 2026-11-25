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

        val status = TextView(this).apply {
            text = "Tu compañera de aventura se está preparando."
            textSize = 15f
            setTextColor(0xFFD5CBE8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 24)
        }

        val overlayButton = Button(this).apply {
            text = "Configurar ventana flotante"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    status.text = "Permiso de ventana flotante concedido."
                } else {
                    status.text = "Activá el permiso para permitir la ventana flotante."
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
    }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 12
        }
}
