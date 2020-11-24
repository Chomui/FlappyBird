package com.chiswstudy.flappybird

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator

class FlappyBird @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val tree: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tree)
    val screenWidth: Float = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .defaultDisplay
        .let {
            val point = Point()
            it.getSize(point)
            point.x.toFloat()
        }

    val paint = Paint().apply {
        isAntiAlias = true
    }
    var left1 = 0F
    var left2 = 0F
    var left3 = 0F

    init {
        moveTrees()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(tree, left1, 0F, paint)
        canvas?.drawBitmap(tree, left2, 0F, paint)
        canvas?.drawBitmap(tree, left3, 0F, paint)
    }

    private fun moveTrees() {
        ValueAnimator.ofFloat(0F, -0.5F, -1F, -1.5F).run {
            duration = 5000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { it ->
                val value = it.animatedValue as Float

                if (value <= 0 && value > -0.5F) {
                    left1 = screenWidth * value
                    left2 = screenWidth * (value + 0.5F)
                    left3 = screenWidth * (value + 1F)
                } else if (value <= -0.5F && value > -1F) {
                    left1 = screenWidth * (value + 1.5F)
                    left2 = screenWidth * (value + 0.5F)
                    left3 = screenWidth * (value + 1F)
                } else if (value <= -1F && value > -1.5F) {
                    left1 = screenWidth * (value + 1.5F)
                    left2 = screenWidth * (value + 2F)
                    left3 = screenWidth * (value + 1F)
                }

                invalidate()
            }
            start()
        }
    }
}