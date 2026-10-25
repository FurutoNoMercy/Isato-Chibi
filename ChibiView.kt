package com.isato.chibi

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

class ChibiView(context: Context, private val onMove: (Float, Float) -> Unit,
                private val onLongPress: () -> Unit) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 51, 81); textSize = 13f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val pictures = mutableMapOf<String, Bitmap>()
    private val started = SystemClock.uptimeMillis()
    private val expressions = listOf("rema_menyapa", "rema_senang", "rema_ketawa", "rema_tidur")
    private var expression = 0
    private var downX = 0f; private var downY = 0f
    private var prevX = 0f; private var prevY = 0f
    private var downTime = 0L; private var moved = false
    private val username = context.getSharedPreferences("rema", Context.MODE_PRIVATE)
        .getString("username", "Teman")?.ifBlank { "Teman" } ?: "Teman"

    init {
        val files = expressions + listOf("portal_open")
        for (file in files) try {
            context.assets.open("$file.png").use { pictures[file] = BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { }
    }
    private fun drawPicture(canvas: Canvas, name: String, rect: RectF, alpha: Int = 255) {
        val image = pictures[name] ?: return
        paint.alpha = alpha.coerceIn(0, 255)
        val scale = minOf(rect.width() / image.width, rect.height() / image.height)
        val dw = image.width * scale; val dh = image.height * scale
        val dst = RectF(rect.centerX()-dw/2, rect.centerY()-dh/2, rect.centerX()+dw/2, rect.centerY()+dh/2)
        canvas.drawBitmap(image, null, dst, paint)
        paint.alpha = 255
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val elapsed = SystemClock.uptimeMillis() - started
        val w = width.toFloat(); val h = height.toFloat()
        val character = RectF(w*.10f, h*.26f, w*.90f, h*.96f)
        if (elapsed < 3400L) {
            val portalAlpha = when {
                elapsed < 450L -> (elapsed / 450f * 255).toInt()
                elapsed > 2650L -> ((3400L - elapsed) / 750f * 255).toInt()
                else -> 255
            }.coerceIn(0, 255)
            drawPicture(canvas, "portal_open", RectF(w*.12f, h*.18f, w*.88f, h*.94f), portalAlpha)
            if (elapsed >= 700L) {
                val progress = ((elapsed - 700L) / 900f).coerceIn(0f, 1f)
                character.offset(0f, (1f - progress) * h * .20f)
                drawPicture(canvas, "rema_menyapa", character, (progress * 255).toInt())
            }
        } else {
            val bob = sin(elapsed / 420.0).toFloat()*3f*resources.displayMetrics.density
            character.offset(0f, bob)
            drawPicture(canvas, expressions[expression], character)
        }
        val message = when (elapsed) {
            in 3600..6700 -> "Haloo! Aku Rema, gadis SMA asal Indonesia!"
            in 7100..10500 -> "Senang bertemu denganmu, Tuan $username!"
            else -> null
        }
        if (message != null) {
            val fade = when {
                elapsed in 3600..4000 -> ((elapsed-3600)/400f)
                elapsed in 6300..6700 -> ((6700-elapsed)/400f)
                elapsed in 7100..7500 -> ((elapsed-7100)/400f)
                elapsed in 10100..10500 -> ((10500-elapsed)/400f)
                else -> 1f
            }.coerceIn(0f,1f)
            paint.color=Color.WHITE; paint.alpha=(235*fade).toInt()
            val bubble=RectF(6f,h*.03f,w-6f,h*.23f)
            canvas.drawRoundRect(bubble,16f,16f,paint)
            textPaint.alpha=(255*fade).toInt()
            val words=message.split(" "); var line=""; val lines=mutableListOf<String>()
            for (word in words) {
                val next=if(line.isEmpty()) word else "$line $word"
                if(textPaint.measureText(next)>w-32f && line.isNotEmpty()) { lines.add(line);line=word } else line=next
            }
            if(line.isNotEmpty())lines.add(line)
            lines.take(3).forEachIndexed { i, str -> canvas.drawText(str, 16f, h*.09f+i*textPaint.textSize*1.2f, textPaint) }
            textPaint.alpha=255;paint.alpha=255
        }
        postInvalidateDelayed(50L)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {downX=event.rawX;downY=event.rawY;prevX=downX;prevY=downY;downTime=SystemClock.uptimeMillis();moved=false;return true}
            MotionEvent.ACTION_MOVE -> {
                val dx=event.rawX-prevX;val dy=event.rawY-prevY
                if(abs(event.rawX-downX)+abs(event.rawY-downY)>14f) moved=true
                if(moved) onMove(dx,dy)
                prevX=event.rawX;prevY=event.rawY;return true
            }
            MotionEvent.ACTION_UP -> {
                if(!moved) {
                    if(SystemClock.uptimeMillis()-downTime>650L) onLongPress()
                    else { expression=(expression+1)%expressions.size;invalidate() }
                }
                return true
            }
        }
        return true
    }
    override fun onDetachedFromWindow() {
        pictures.values.forEach { if(!it.isRecycled) it.recycle() }
        pictures.clear()
        super.onDetachedFromWindow()
    }
}
