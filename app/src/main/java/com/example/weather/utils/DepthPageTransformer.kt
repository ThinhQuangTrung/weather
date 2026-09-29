package com.example.weather.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

private const val MIN_SCALE = 0.75f

/**
 * DepthPageTransformer tạo hiệu ứng chiều sâu (depth transition) khi vuốt giữa các trang Onboarding.
 * - Trang bên trái trượt bình thường theo hướng vuốt.
 * - Trang bên phải mờ dần và thu nhỏ lại, nằm ẩn phía dưới trang bên trái.
 */
class DepthPageTransformer(
    private val minScale: Float = MIN_SCALE
) : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // Trang hoàn toàn lệch về bên trái màn hình
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Giữ hiệu ứng trượt mặc định cho trang bên trái
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Làm mờ dần trang
                    alpha = 1 - position

                    // Triệt tiêu hiệu ứng trượt mặc định để tạo cảm giác trang nằm cố định phía sau
                    translationX = pageWidth * -position
                    // Đưa trang xuống dưới trang bên trái
                    translationZ = -1f

                    // Thu nhỏ kích thước trang (giữa minScale và 1)
                    val scaleFactor = (minScale + (1 - minScale) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // Trang hoàn toàn lệch về bên phải màn hình
                    alpha = 0f
                }
            }
        }
    }
}
