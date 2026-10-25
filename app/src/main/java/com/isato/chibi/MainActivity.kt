package com.isato.chibi

import android.Manifest
import android.app.*
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(44, 80, 44, 36)
            setBackgroundColor(Color.rgb(241, 247, 255))
        }
        fun label(s: String, size: Float): TextView = TextView(this).apply {
            text=s; textSize=size; setTextColor(Color.rgb(35,55,88))
            setPadding(0, 0, 0, 25)
        }
        layout.addView(label("💙 Isato Chibi Assistant", 25f))
        layout.addView(label("Beta 2D • Karakter melayang di atas aplikasi lain", 16f))
        status=label("Status: siap", 15f)
        layout.addView(status)
        fun button(title:String, action:()->Unit) {
            val b=Button(this).apply { text=title; setOnClickListener { action() } }
            layout.addView(b)
        }
        button("▶ Tampilkan karakter") { startFloating() }
        button("■ Sembunyikan karakter") {
            stopService(Intent(this, FloatingService::class.java))
            status.text="Status: karakter disembunyikan"
        }
        button("⚙ Buka izin tampil di atas aplikasi") { openOverlaySettings() }
        layout.addView(label("Cara pakai: izinkan 'Tampilkan di atas aplikasi lain', lalu tekan Tampilkan karakter. Geser Rem dengan jari. Ketuk Rem untuk mengganti ekspresi. Tekan lama untuk menyembunyikan.",14f))
        setContentView(ScrollView(this).apply { addView(layout) })
    }
    private fun openOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")))
    }
    private fun startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            status.text="Aktifkan izin overlay dulu, lalu kembali ke aplikasi."
            openOverlaySettings()
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        try {
            startForegroundService(Intent(this, FloatingService::class.java))
            status.text="Status: karakter aktif. Coba buka Home!"
        } catch(e:Exception) { status.text="Gagal menjalankan: ${e.message}" }
    }
}
