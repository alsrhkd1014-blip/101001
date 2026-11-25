package com.example.eggmacro

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout

class TapService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private val tapLoop = object : Runnable {
        override fun run() {
            if (!running) return
            val p = getSharedPreferences("settings", MODE_PRIVATE)
            val x = p.getInt("x", 540); val y = p.getInt("y", 1400)
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            dispatchGesture(android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 70)).build(), null, null)
            handler.postDelayed(this, p.getLong("interval", 1200L).coerceAtLeast(500L))
        }
    }
    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (panel == null) {
            val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 8, 12, 8); setBackgroundColor(0xEE222222.toInt()) }
            box.addView(Button(this).apply { text = "START"; setOnClickListener {
                running = !running
                if (running) { handler.removeCallbacks(tapLoop); handler.post(tapLoop); (it as Button).text = "RUNNING · STOP" }
                else { handler.removeCallbacks(tapLoop); (it as Button).text = "START" }
            } })
            box.addView(Button(this).apply { text = "닫기"; setOnClickListener { running = false; handler.removeCallbacks(tapLoop); try { wm.removeView(box) } catch (_: Exception) {}; panel = null } })
            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 20; y = 180 }
            wm.addView(box, params); panel = box
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { running = false; handler.removeCallbacks(tapLoop) }
    override fun onDestroy() { running = false; handler.removeCallbacks(tapLoop); panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }; panel = null; super.onDestroy() }
}
