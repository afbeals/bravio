package dev.tvlauncher

import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Idle screensaver: a dim, drifting clock on black. Plain Views (no Compose) so it costs almost no memory.
 * Enable it with `settings put secure screensaver_components dev.tvlauncher/.ClockDreamService`.
 */
class ClockDreamService : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var root: FrameLayout
    private lateinit var box: LinearLayout
    private lateinit var time: TextView
    private lateinit var date: TextView
    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, 60_000L - System.currentTimeMillis() % 60_000L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        time = text(96f, Color.rgb(200, 200, 200), Typeface.DEFAULT_BOLD)
        date = text(28f, Color.rgb(140, 140, 140), Typeface.DEFAULT)
        box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(time)
            addView(date)
        }
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(box, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        tick.run()
    }

    override fun onDreamingStopped() {
        handler.removeCallbacks(tick)
        super.onDreamingStopped()
    }

    private fun text(sizeSp: Float, color: Int, face: Typeface) = TextView(this).apply {
        textSize = sizeSp
        setTextColor(color)
        typeface = face
        gravity = Gravity.CENTER
    }

    private fun render() {
        val profile = ConfigStore(this).load().activeProfile
        val now = LocalDateTime.now()
        time.text = now.format(DateTimeFormatter.ofPattern(if (profile.clock24h) "HH:mm" else "h:mm a"))
        date.text = runCatching { now.format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("")
        date.visibility = if (profile.showDate) View.VISIBLE else View.GONE
        root.post {
            val (fx, fy) = driftFraction(now.hour * 60 + now.minute)
            box.translationX = fx * (root.width - box.width).coerceAtLeast(0)
            box.translationY = fy * (root.height - box.height).coerceAtLeast(0)
        }
    }
}
