package dev.tvlauncher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Idle screensaver: the drifting clock of the design (180 px time, 30 px date). Plain Views (no Compose) so
 * it costs almost no memory. Enable it with
 * `settings put secure screensaver_components dev.tvlauncher/.ClockDreamService`.
 */
class ClockDreamService : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var root: FrameLayout
    private lateinit var box: LinearLayout
    private lateinit var time: TextView
    private lateinit var date: TextView
    private var picture: Bitmap? = null
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
        val inter = ResourcesCompat.getFont(this, R.font.inter) ?: Typeface.DEFAULT
        time = text(180f, weight(inter, 300))
        date = text(30f, weight(inter, 600))
        box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(time)
            addView(date)
        }
        root = FrameLayout(this).apply {
            addView(box, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        }
        setContentView(root)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        val config = ConfigStore(this).load()
        picture = if (config.screensaverWallpaper) loadPicture(config.activeProfile.wallpaper) else null
        tick.run()
    }

    override fun onDreamingStopped() {
        handler.removeCallbacks(tick)
        picture = null
        super.onDreamingStopped()
    }

    // Design pixels are screen pixels on this 1080p TV, so sizes are given in px.
    private fun text(sizePx: Float, face: Typeface) = TextView(this).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
        typeface = face
        gravity = Gravity.CENTER
        includeFontPadding = false
    }

    private fun weight(base: Typeface, weight: Int): Typeface =
        if (Build.VERSION.SDK_INT >= 28) Typeface.create(base, weight, false) else base

    private fun loadPicture(id: String): Bitmap? {
        if (!isPhotoWallpaper(id)) return null
        val preset = wallpaperPreset(id)
        return runCatching {
            if (isFileWallpaper(id)) {
                wallpaperFile(this, id)?.let { decodeWallpaper(it) }
            } else {
                BitmapFactory.decodeResource(resources, preset.photo, BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 })
            }
        }.getOrNull()
    }

    private fun render() {
        val config = ConfigStore(this).load()
        val profile = config.activeProfile
        val now = LocalDateTime.now()
        val dim = config.screensaverDim && isNightHour(now.hour)
        val textColor = Color.argb(255, if (dim) 0xC6 else 0xF4, if (dim) 0xBB else 0xF0, if (dim) 0xD7 else 0xFA)
        val mutedColor = Color.argb(255, if (dim) 0xB2 else 0xBD, if (dim) 0xA7 else 0xB0, if (dim) 0xC4 else 0xD0)
        val bg = if (dim) Color.rgb(0x0B, 0x09, 0x10) else Color.rgb(0x14, 0x11, 0x1C)
        time.setTextColor(textColor)
        date.setTextColor(mutedColor)
        time.text = now.format(DateTimeFormatter.ofPattern(if (profile.clock24h) "HH:mm" else "h:mm a"))
        date.text = runCatching { now.format(DateTimeFormatter.ofPattern(profile.datePattern)) }.getOrDefault("")
        date.visibility = if (profile.showDate) View.VISIBLE else View.GONE

        val shot = picture
        root.setBackgroundColor(bg)
        root.removeAllViews()
        if (shot != null) {
            root.addView(
                ImageView(this).apply { setImageBitmap(shot); scaleType = ImageView.ScaleType.CENTER_CROP },
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
            )
            root.addView(View(this).apply { setBackgroundColor(Color.argb(0xCC, 0, 0, 0)) }, FrameLayout.LayoutParams(-1, -1))
            box.background = GradientDrawable().apply { setColor(bg); cornerRadius = 24f }
            box.minimumWidth = 1040
            box.minimumHeight = 360
        } else {
            box.background = null
        }
        (box.parent as? FrameLayout)?.removeView(box)
        root.addView(box, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        val (dx, dy) = driftOffset(now.hour * 60 + now.minute)
        box.translationX = dx
        box.translationY = dy
    }
}
