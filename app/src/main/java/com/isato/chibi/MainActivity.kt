package com.isato.chibi

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val prefs by lazy { getSharedPreferences("rema", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("rema_settings", MODE_PRIVATE)

if (!prefs.getBoolean("beta_notice_shown", false)) {
    AlertDialog.Builder(this)
        .setTitle("Rema Assistant — BETA v0.2")
        .setMessage(
            "⚠️ Aplikasi Masih Dalam Pengembangan\n\n" +
            "Halo! Selamat datang di Rema Assistant.\n\n" +
            "Aplikasi ini masih dalam tahap pembuatan dan pengujian. " +
            "Beberapa fitur mungkin belum berjalan dengan sempurna.\n\n" +
            "Arigathanks! 💙\n\n" +
            "Made by IsatoPrjj"
        )
        .setPositiveButton("Mengerti") { _, _ ->
            prefs.edit()
                .putBoolean("beta_notice_shown", true)
                .apply()
        }
        .show()
}
        val layout=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(24,40,24,32)
            setBackgroundColor(Color.rgb(241,247,255))
        }
        fun label(value:String, size:Float):TextView = TextView(this).apply {
            text=value;textSize=size;setTextColor(Color.rgb(35,55,88));setPadding(0,4,0,12)
        }
        layout.addView(label("Isato Project — Rema V0.2",24f))
        layout.addView(label("Nama panggilan:",15f))
        val name=EditText(this).apply {
            setSingleLine(true);setText(prefs.getString("username",""));hint="Masukkan nama kamu"
        }
        layout.addView(name)
        layout.addView(Button(this).apply {
            text="Simpan nama";setOnClickListener {
                prefs.edit().putString("username",name.text.toString().trim().take(32)).apply()
                Toast.makeText(this@MainActivity,"Nama disimpan",Toast.LENGTH_SHORT).show()
            }
        })
        status=label("Status: siap",14f);layout.addView(status)
        layout.addView(Button(this).apply { text="✨ Bawa Rema keluar aplikasi";setOnClickListener { startFloating(name.text.toString()) } })
        layout.addView(Button(this).apply {
            text="■ Sembunyikan Rema";setOnClickListener {
                stopService(Intent(this@MainActivity,FloatingService::class.java));status.text="Status: Rema disembunyikan"
            }
        })
        layout.addView(Button(this).apply {text="⚙ Izin tampil di atas aplikasi";setOnClickListener {openOverlaySettings()} })
        layout.addView(label("Percakapan lokal (belum tersambung AI)",18f))
        val history=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
        val scroll=ScrollView(this).apply {addView(history)}
        layout.addView(scroll,LinearLayout.LayoutParams(-1,260))
        val chat=JSONArray(prefs.getString("chat","[]"))
        fun render() {
            history.removeAllViews()
            for(i in 0 until chat.length()) {
                val entry=chat.optJSONObject(i) ?: continue
                history.addView(label("${entry.optString("who")}: ${entry.optString("text")}",15f))
            }
            scroll.post {scroll.fullScroll(ScrollView.FOCUS_DOWN)}
        }
        render()
        val row=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val input=EditText(this).apply {hint="Tulis pesan...";setSingleLine(true)}
        row.addView(input,LinearLayout.LayoutParams(0,-2,1f))
        row.addView(Button(this).apply {
            text="Kirim";setOnClickListener {
                val msg=input.text.toString().trim();if(msg.isEmpty())return@setOnClickListener
                chat.put(JSONObject().put("who","Kamu").put("text",msg))
                val reply=when {
                    msg.contains("belajar",true) -> "Wah, semangat belajarnya! Kalau ada yang nggak paham, bilang yaa."
                    msg.contains("halo",true) || msg.contains("hai",true) -> "Haloo! Senang ketemu lagi!"
                    else -> "Aku dengar, nih! Fitur AI akan ditambahkan di update berikutnya."
                }
                chat.put(JSONObject().put("who","Rema").put("text",reply))
                prefs.edit().putString("chat",chat.toString()).apply()
                input.setText("");render()
            }
        });layout.addView(row)
        layout.addView(label("Ketuk Rema untuk ganti ekspresi • geser untuk memindahkan • tekan lama untuk menutup.",13f))
        setContentView(ScrollView(this).apply {addView(layout)})
    }
    private fun openOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
    }
    private fun startFloating(username:String) {
        prefs.edit().putString("username",username.trim().take(32)).apply()
        if(!Settings.canDrawOverlays(this)) {status.text="Aktifkan izin overlay dulu";openOverlaySettings();return}
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),101)
        }
        try {
            stopService(Intent(this,FloatingService::class.java))
            startForegroundService(Intent(this,FloatingService::class.java))
            status.text="Rema aktif! Buka Home untuk melihatnya."
        } catch(e:Exception) {status.text="Gagal: ${e.message}"}
    }
}
