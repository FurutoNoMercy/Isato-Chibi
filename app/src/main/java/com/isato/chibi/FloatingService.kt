package com.isato.chibi

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.provider.Settings

class FloatingService : Service() {
    private var wm: WindowManager?=null
    private var pet: ChibiView?=null
    private var params: WindowManager.LayoutParams?=null
    private val channel="isato_floating"
    override fun onBind(intent:Intent?)=null
    override fun onCreate() {
        super.onCreate()
        val nm=getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(channel,"Isato floating character",
            NotificationManager.IMPORTANCE_LOW))
        val stopIntent=Intent(this, FloatingService::class.java).apply { action="STOP" }
        val stopPending=PendingIntent.getService(this,0,stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification=Notification.Builder(this,channel)
            .setContentTitle("Isato Chibi aktif")
            .setContentText("Karakter sedang tampil di layar")
            .setSmallIcon(android.R.drawable.star_on)
            .addAction(Notification.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,"Sembunyikan",stopPending).build())
            .build()
        startForeground(7,notification)
        if (!Settings.canDrawOverlays(this)) { stopSelf();return }
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        params=WindowManager.LayoutParams(
            dp(172),dp(240),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity=Gravity.TOP or Gravity.START; x=40; y=250 }
        pet=ChibiView(this,
            onMove={dx,dy ->
                params?.let { p ->
                    p.x+=dx.toInt();p.y+=dy.toInt()
                    try { wm?.updateViewLayout(pet,p) } catch (_:Exception) {}
                }
            },
            onLongPress={stopSelf()})
        try { wm?.addView(pet,params) } catch (_:Exception) { stopSelf() }
    }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if (intent?.action=="STOP") {stopSelf();return START_NOT_STICKY}
        return START_STICKY
    }
    override fun onDestroy() {
        pet?.let { try { wm?.removeView(it) } catch (_:Exception) {} }
        pet=null
        super.onDestroy()
    }
}
