package com.isato.chibi

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

class ChibiView(context:Context, val onMove:(Float,Float)->Unit,
    val onLongPress:()->Unit):View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val blue=Color.rgb(127,178,241)
    private val pink=Color.rgb(245,139,179)
    private val skin=Color.rgb(255,225,211)
    private val navy=Color.rgb(58,72,109)
    private var expression=0
    private var downX=0f; private var downY=0f
    private var prevX=0f; private var prevY=0f
    private var downAt=0L;private var dragged=false
    private val start=System.currentTimeMillis()
    private fun color(c:Int){paint.color=c;paint.style=Paint.Style.FILL;paint.strokeWidth=1f}
    private fun oval(c:Canvas,l:Float,t:Float,r:Float,b:Float,col:Int){
        color(col);c.drawOval(l,t,r,b,paint)
    }
    private fun line(c:Canvas,x:Float,y:Float,x2:Float,y2:Float,col:Int,w:Float){
        color(col);paint.strokeWidth=w;paint.strokeCap=Paint.Cap.ROUND
        c.drawLine(x,y,x2,y2,paint)
    }
    private fun path(c:Canvas, col:Int, vararg pts:Float){
        val p=Path();p.moveTo(pts[0],pts[1])
        for(i in 2 until pts.size step 2)p.lineTo(pts[i],pts[i+1])
        p.close();color(col);c.drawPath(p,paint)
    }
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        val time=(System.currentTimeMillis()-start)/1000f
        val w=width/172f;val h=height/240f
        canvas.save();canvas.scale(w,h)
        val bounce=sin(time*2.5f)*2.2f
        canvas.translate(0f,bounce)
        // hair behind head
        oval(canvas,29f,17f,146f,148f,blue)
        // legs and shoes
        oval(canvas,67f,188f,82f,220f,skin);oval(canvas,94f,188f,109f,220f,skin)
        oval(canvas,57f,213f,84f,229f,navy);oval(canvas,92f,213f,120f,229f,navy)
        // pleated skirt
        path(canvas,Color.rgb(106,121,154),52f,163f,122f,163f,134f,194f,40f,194f)
        for(x in listOf(57f,75f,95f,114f))
            line(canvas,x,169f,x+(x-87f)*.18f,191f,Color.rgb(78,91,130),1.6f)
        // arms
        oval(canvas,35f,146f,57f,193f,skin);oval(canvas,119f,146f,141f,193f,skin)
        // shirt
        path(canvas,Color.WHITE,56f,137f,117f,137f,128f,173f,45f,173f)
        path(canvas,Color.rgb(228,233,246),75f,139f,87f,156f,99f,139f)
        path(canvas,navy,85f,150f,94f,150f,91f,169f,86f,172f,82f,169f)
        // head
        oval(canvas,34f,31f,141f,146f,skin)
        // side hair
        oval(canvas,27f,57f,52f,148f,blue);oval(canvas,124f,57f,150f,148f,blue)
        // eyes
        val blink=(time%4.5f)>4.34f
        for(x in listOf(65f,112f)) {
            oval(canvas,x-12,89f,x+12,112f,Color.WHITE)
            if(blink)line(canvas,x-10,101f,x+10,101f,navy,3f)
            else {
                oval(canvas,x-7,92f,x+7,111f,Color.rgb(60,128,207))
                oval(canvas,x-3,95f,x+3,106f,navy)
                oval(canvas,x-5,94f,x-1,99f,Color.WHITE)
            }
        }
        oval(canvas,83f,115f,92f,119f,Color.rgb(246,167,174))
        if(expression==1){
            oval(canvas,82f,125f,94f,137f,Color.rgb(171,79,107))
        } else if(expression==2) {
            line(canvas,81f,133f,94f,130f,Color.rgb(150,75,96),2.5f)
        } else {
            line(canvas,80f,127f,87f,132f,Color.rgb(175,88,111),2.5f)
            line(canvas,87f,132f,96f,127f,Color.rgb(175,88,111),2.5f)
        }
        // hair cap and asymmetrical fringe
        oval(canvas,28f,17f,146f,73f,blue)
        path(canvas,blue,28f,55f,70f,28f,100f,36f,80f,92f,39f,91f)
        path(canvas,Color.rgb(150,198,249),96f,28f,142f,49f,130f,106f,99f,97f,108f,62f)
        // pink cross clip
        line(canvas,112f,52f,133f,73f,pink,5f)
        line(canvas,133f,52f,112f,73f,pink,5f)
        // ribbon
        oval(canvas,132f,85f,148f,98f,pink)
        oval(canvas,146f,85f,160f,98f,pink)
        oval(canvas,143f,87f,150f,96f,Color.rgb(232,105,158))
        // flower
        for(i in 0..4){
            val a=i*2*PI/5
            oval(canvas,(46+cos(a)*9-6).toFloat(),(43+sin(a)*9-6).toFloat(),
                (46+cos(a)*9+6).toFloat(),(43+sin(a)*9+6).toFloat(),Color.WHITE)
        }
        oval(canvas,42f,39f,50f,47f,Color.rgb(246,209,107))
        canvas.restore()
        postInvalidateDelayed(33)
    }
    override fun onTouchEvent(e:android.view.MotionEvent):Boolean {
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                downX=e.rawX;downY=e.rawY;prevX=e.rawX;prevY=e.rawY
                downAt=System.currentTimeMillis();dragged=false;return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx=e.rawX-prevX;val dy=e.rawY-prevY
                if(abs(e.rawX-downX)+abs(e.rawY-downY)>12)dragged=true
                if(dragged)onMove(dx,dy)
                prevX=e.rawX;prevY=e.rawY;return true
            }
            MotionEvent.ACTION_UP -> {
                if(!dragged) {
                    if(System.currentTimeMillis()-downAt>650)onLongPress()
                    else {expression=(expression+1)%3;invalidate()}
                }
                return true
            }
        }
        return true
    }
}
