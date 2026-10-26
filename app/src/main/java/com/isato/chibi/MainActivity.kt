package com.isato.chibi

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("rema", MODE_PRIVATE) }
    private val navy = Color.rgb(34, 57, 93)
    private val blue = Color.rgb(95, 151, 223)
    private val pale = Color.rgb(241, 247, 255)
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private lateinit var name: EditText
    private var currentTab = 0
    private val requestCharacter = 201
    private val requestBackground = 202
    private val chat by lazy {
        try { JSONArray(prefs.getString("chat", "[]")) } catch (_: Exception) { JSONArray() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val beta = getSharedPreferences("rema_settings", MODE_PRIVATE)
        if (!beta.getBoolean("beta_notice_v043", false)) {
            AlertDialog.Builder(this)
                .setTitle("Rema Assistant — BETA v0.4.3")
                .setMessage("Wardrobe dan background gambar kini bisa dicoba. Chat masih lokal, belum tersambung AI.\n\nMade by IsatoPrjj 💙")
                .setPositiveButton("Mengerti") { _, _ ->
                    beta.edit().putBoolean("beta_notice_v043", true).apply()
                }.show()
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pale)
            setPadding(dp(14), dp(16), dp(14), dp(12))
        }
        root.addView(text("💙 Rema Assistant", 23f, true).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, dp(48)))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("💬 Chat", "👗 Wardrobe", "🎮 Game", "⚙ Settings").forEachIndexed { index, label ->
            tabs.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                setOnClickListener { showTab(index) }
            }, LinearLayout.LayoutParams(0, dp(54), 1f))
        }
        root.addView(tabs)
        setContentView(root)
        showTab(0)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float = 15f, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(navy)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(dp(8), dp(6), dp(8), dp(6))
    }
    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(17).toFloat()
    }
    private fun showTab(index: Int) {
        currentTab = index
        content.removeAllViews()
        when (index) {
            0 -> showChat()
            1 -> showWardrobe()
            2 -> showPlaceholder("🎮 Game & App Library", "Daftar game, perintah membuka aplikasi, dan rekomendasi akan ditambahkan pada tahap AI Beta.")
            else -> showSettings()
        }
    }
    private fun showPlaceholder(title: String, desc: String) {
        content.addView(text(title, 22f, true))
        content.addView(text(desc, 15f))
        content.addView(text("Fitur ini belum aktif.", 13f))
    }

    private fun loadImage(view: ImageView, customFile: String?, asset: String): Boolean {
        if (!customFile.isNullOrEmpty()) {
            try {
                val file = File(filesDir, customFile)
                if (file.isFile) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) { view.setImageBitmap(bitmap); return true }
                }
            } catch (_: Exception) { }
        }
        return try {
            assets.open(asset).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) { view.setImageBitmap(bitmap); true } else false
            }
        } catch (_: Exception) { false }
    }
    private fun characterImage(): ImageView = ImageView(this).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        val custom = if (prefs.getString("costume", "default") == "custom") "rema_custom.png" else null
        if (!loadImage(this, custom, "rema_menyapa.png")) {
            setImageResource(android.R.drawable.ic_menu_gallery)
        }
        contentDescription = "Tampilan karakter Rema"
    }
    private fun backgroundImage(): ImageView = ImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        if (!loadImage(this, "rema_background.jpg", "rema_background_default.png")) {
            setBackgroundColor(Color.rgb(218, 235, 255))
        }
        contentDescription = "Latar belakang Rema"
    }

    private fun showWardrobe() {
        val scroll = ScrollView(this)
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(4), dp(6), dp(16))
        }
        scroll.addView(panel)
        content.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        panel.addView(text("👗 Wardrobe Rema", 22f, true))
        panel.addView(text("Pilih tampilan Rema atau upload PNG dari galeri HP.", 14f))
        panel.addView(characterImage(), LinearLayout.LayoutParams(-1, dp(220)))
        panel.addView(text("Tampilan aktif: " + if (prefs.getString("costume", "default") == "custom") "PNG custom" else "Rema bawaan", 14f, true))
        panel.addView(Button(this).apply {
            text = "💙 Gunakan Rema bawaan"
            isAllCaps = false
            setOnClickListener {
                prefs.edit().putString("costume", "default").apply()
                showTab(1)
            }
        })
        panel.addView(Button(this).apply {
            text = "📁 Upload karakter PNG"
            isAllCaps = false
            setOnClickListener { openImagePicker(requestCharacter, "image/png") }
        })
        panel.addView(text("PNG custom mengganti seluruh gambar karakter. Kostum berlapis dan koleksi tambahan menyusul.", 13f))
        panel.addView(text("🖼 Background", 20f, true))
        panel.addView(backgroundImage(), LinearLayout.LayoutParams(-1, dp(130)))
        panel.addView(Button(this).apply {
            text = "📷 Pilih background dari galeri"
            isAllCaps = false
            setOnClickListener { openImagePicker(requestBackground, "image/*") }
        })
        panel.addView(Button(this).apply {
            text = "↩ Reset background"
            isAllCaps = false
            setOnClickListener {
                File(filesDir, "rema_background.jpg").delete()
                showTab(1)
            }
        })
        panel.addView(text("Background tampil di halaman Chat dan preview Wardrobe. Background video MP4 menyusul.", 13f))
    }

    private fun openImagePicker(requestCode: Int, mime: String) {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
            }
            startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Toast.makeText(this, "Galeri tidak bisa dibuka: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Uses the Activity result API supported by this project")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri = data.data ?: return
        val filename = when (requestCode) {
            requestCharacter -> "rema_custom.png"
            requestBackground -> "rema_background.jpg"
            else -> return
        }
        try {
            // Decode first, then store a local, bounded-size image accessible by FloatingService.
            val source = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: throw IllegalArgumentException("Gambar tidak dapat dibaca")
            val maxSide = 1600
            val scale = minOf(1f, maxSide.toFloat() / maxOf(source.width, source.height))
            val bitmap = if (scale < 1f) {
                android.graphics.Bitmap.createScaledBitmap(source,
                    (source.width * scale).toInt().coerceAtLeast(1),
                    (source.height * scale).toInt().coerceAtLeast(1), true)
            } else source
            File(filesDir, filename).outputStream().use { out ->
                val format = if (requestCode == requestCharacter)
                    android.graphics.Bitmap.CompressFormat.PNG else android.graphics.Bitmap.CompressFormat.JPEG
                if (!bitmap.compress(format, 90, out)) throw IllegalStateException("Gagal menyimpan gambar")
            }
            if (bitmap !== source) bitmap.recycle()
            source.recycle()
            if (requestCode == requestCharacter) prefs.edit().putString("costume", "custom").apply()
            Toast.makeText(this, "Gambar berhasil disimpan!", Toast.LENGTH_SHORT).show()
            showTab(currentTab)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showChat() {
        val header = FrameLayout(this)
        header.addView(backgroundImage(), FrameLayout.LayoutParams(-1, -1))
        header.addView(characterImage(), FrameLayout.LayoutParams(-1, -1))
        content.addView(header, LinearLayout.LayoutParams(-1, dp(185)))
        content.addView(text("Rema • Chat lokal", 15f, true).apply { gravity = Gravity.CENTER })
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        scroll.addView(messages)
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        fun render() {
            messages.removeAllViews()
            if (chat.length() == 0) messages.addView(text("Haloo! Aku Rema! Yoroshiku ne! 💙"))
            for (i in 0 until chat.length()) {
                val entry = chat.optJSONObject(i) ?: continue
                val mine = entry.optString("who") == "Kamu"
                val line = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = if (mine) Gravity.END else Gravity.START
                }
                val bubble = text(entry.optString("text"), 14f).apply {
                    setTextColor(if (mine) Color.WHITE else navy)
                    background = rounded(if (mine) blue else Color.WHITE)
                    setPadding(dp(12), dp(9), dp(12), dp(9))
                    maxWidth = resources.displayMetrics.widthPixels * 3 / 4
                }
                line.addView(bubble)
                messages.addView(line, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(7) })
            }
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
        render()
        val sendRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val input = EditText(this).apply {
            hint = "Ketik pesan ke Rema..."
            setSingleLine(true)
            textSize = 14f
        }
        sendRow.addView(input, LinearLayout.LayoutParams(0, dp(55), 1f))
        sendRow.addView(Button(this).apply {
            text = "Kirim"
            isAllCaps = false
            setOnClickListener {
                val msg = input.text.toString().trim()
                if (msg.isEmpty()) return@setOnClickListener
                chat.put(JSONObject().put("who", "Kamu").put("text", msg))
                val reply = when {
                    msg.contains("belajar", true) -> "Wah, semangat belajarnya! Kalau ada yang nggak paham, bilang yaa."
                    msg.contains("halo", true) || msg.contains("hai", true) -> "Haloo! Senang ketemu lagi!"
                    else -> "Aku dengar, nih! Fitur AI akan ditambahkan di update berikutnya."
                }
                chat.put(JSONObject().put("who", "Rema").put("text", reply))
                prefs.edit().putString("chat", chat.toString()).apply()
                input.setText("")
                render()
            }
        }, LinearLayout.LayoutParams(dp(86), dp(55)))
        content.addView(sendRow)
    }

    private fun showSettings() {
        val scroll = ScrollView(this)
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(panel)
        content.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        panel.addView(text("⚙ Pengaturan Rema", 22f, true))
        panel.addView(text("Nama panggilan:"))
        name = EditText(this).apply {
            setSingleLine(true)
            setText(prefs.getString("username", ""))
            hint = "Masukkan nama kamu"
        }
        panel.addView(name)
        panel.addView(Button(this).apply {
            text = "Simpan nama"
            setOnClickListener {
                prefs.edit().putString("username", name.text.toString().trim().take(32)).apply()
                Toast.makeText(this@MainActivity, "Nama disimpan", Toast.LENGTH_SHORT).show()
            }
        })
        status = text("Status: siap", 14f)
        panel.addView(status)
        panel.addView(Button(this).apply {
            text = "✨ Bawa Rema keluar aplikasi"
            setOnClickListener { startFloating(name.text.toString()) }
        })
        panel.addView(Button(this).apply {
            text = "■ Sembunyikan Rema"
            setOnClickListener {
                stopService(Intent(this@MainActivity, FloatingService::class.java))
                status.text = "Status: Rema disembunyikan"
            }
        })
        panel.addView(Button(this).apply {
            text = "⚙ Izin tampil di atas aplikasi"
            setOnClickListener { openOverlaySettings() }
        })
        panel.addView(text("Hybrid Voice dan pengaturan animasi akan hadir pada update berikutnya.", 13f))
    }
    private fun openOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }
    private fun startFloating(username: String) {
        prefs.edit().putString("username", username.trim().take(32)).apply()
        if (!Settings.canDrawOverlays(this)) {
            status.text = "Aktifkan izin overlay dulu"
            openOverlaySettings()
            return
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        try {
            stopService(Intent(this, FloatingService::class.java))
            startForegroundService(Intent(this, FloatingService::class.java))
            status.text = "Rema aktif! Buka Home untuk melihatnya."
        } catch (e: Exception) { status.text = "Gagal: ${e.message}" }
    }
}
