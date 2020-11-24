package com.chiswstudy.flappybird

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator


class FlappyBird @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val tree: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tree)
    val reversedTree: Bitmap = tree.createFlippedBitmap(xFlip = false, yFlip = true)

    /*val screenWidth: Float = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .defaultDisplay
        .let {
            val point = Point()
            it.getSize(point)
            point.x.toFloat()
        }
    val screenHeight: Float = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .defaultDisplay
        .let {
            val point = Point()
            it.getSize(point)
            point.y.toFloat()
        }*/
    var viewWidth = 0F
    var viewHeight = 0F

    val paint = Paint().apply {
        isAntiAlias = true
    }

    var left1 = 0F
    var left2 = 0F
    var left3 = 0F
    val treeSpace = 0.8F

    init {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewHeight = height.toFloat()
                viewWidth = width.toFloat()
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                moveTrees()
            }
        })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(tree, left1, viewHeight - (tree.height / 2), paint)
        canvas?.drawBitmap(reversedTree, left1, 0F - (tree.height / 2), paint)

        canvas?.drawBitmap(tree, left2, viewHeight - (tree.height / 2), paint)
        canvas?.drawBitmap(reversedTree, left2, 0F - (tree.height / 2), paint)

        canvas?.drawBitmap(tree, left3, viewHeight - (tree.height / 2), paint)
        canvas?.drawBitmap(reversedTree, left3, 0F - (tree.height / 2), paint)
    }

    private fun moveTrees() {
        ValueAnimator.ofFloat(0F, -treeSpace, -2 * treeSpace, -3 * treeSpace).run {
            duration = 5000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { it ->
                val value = it.animatedValue as Float

                if (value <= 0 && value > -treeSpace) {
                    left1 = viewWidth * value
                    left2 = viewWidth * (value + treeSpace)
                    left3 = viewWidth * (value + 2 * treeSpace)
                } else if (value <= -treeSpace && value > -2 * treeSpace) {
                    left1 = viewWidth * (value + 3 * treeSpace)
                    left2 = viewWidth * (value + treeSpace)
                    left3 = viewWidth * (value + 2 * treeSpace)
                } else if (value <= -2 * treeSpace && value > -3 * treeSpace) {
                    left1 = viewWidth * (value + 3 * treeSpace)
                    left2 = viewWidth * (value + 4 * treeSpace)
                    left3 = viewWidth * (value + 2 * treeSpace)
                }

                invalidate()
            }
            start()
        }
    }

    private fun Bitmap.createFlippedBitmap(xFlip: Boolean, yFlip: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.postScale(
            if (xFlip) -1F else 1F,
            if (yFlip) -1F else 1F,
            this.width / 2F,
            this.height / 2F
        )
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }
}