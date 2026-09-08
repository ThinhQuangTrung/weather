package com.example.weather.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.weather.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom View vẽ cung tròn quỹ đạo mặt trời (Sun & Day Cycle arc):
 * - Vẽ đường cong nét đứt thể hiện quỹ đạo từ Bình minh đến Hoàng hôn.
 * - Vẽ vòng tròn mặt trời định vị thời gian hiện tại trên quỹ đạo.
 */
class SunPathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0.5f // 0.0 (Sunrise) -> 1.0 (Sunset)

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.sun_arc)
        style = Paint.Style.STROKE
        strokeWidth = 4f * resources.displayMetrics.density
        pathEffect = DashPathEffect(floatArrayOf(12f, 10f), 0f)
    }

    private val sunOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.badge_orange_bg)
        style = Paint.Style.FILL
    }

    private val sunStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.sun_arc)
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
    }

    private val sunCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.badge_orange_icon)
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val arcRect = RectF()

    /**
     * Cập nhật tiến độ vị trí mặt trời (từ 0.0 đến 1.0)
     */
    fun setSunProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0 || height <= 0) return

        val padding = 28f * resources.displayMetrics.density
        val left = padding
        val right = width - padding
        val bottom = height - (10f * resources.displayMetrics.density)
        val top = 10f * resources.displayMetrics.density

        arcRect.set(left, top, right, bottom * 1.8f)

        path.reset()
        path.addArc(arcRect, 180f, 180f)
        canvas.drawPath(path, arcPaint)

        // Tính vị trí tâm mặt trời trên cung tròn
        val angleRad = (180.0 + progress * 180.0) * (PI / 180.0)
        val rx = (arcRect.width()) / 2f
        val ry = (arcRect.height()) / 2f
        val cx = arcRect.centerX() + rx * cos(angleRad).toFloat()
        val cy = arcRect.centerY() + ry * sin(angleRad).toFloat()

        // Vẽ vầng sáng hào quang mặt trời
        val outerRadius = 12f * resources.displayMetrics.density
        val centerRadius = 5f * resources.displayMetrics.density
        canvas.drawCircle(cx, cy, outerRadius, sunOuterPaint)
        canvas.drawCircle(cx, cy, outerRadius, sunStrokePaint)
        canvas.drawCircle(cx, cy, centerRadius, sunCenterPaint)
    }
}
