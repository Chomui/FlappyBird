package com.chiswstudy.flappybird

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.random.Random


class FlappyBird @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val tree: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.tree).let {
            Bitmap.createScaledBitmap(
                it,
                it.width / 2,
                it.height / 2,
                false
            )
        }

    private val reversedTree: Bitmap = tree.createFlippedBitmap(xFlip = false, yFlip = true)
    private val bird: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(context.resources, R.drawable.bird),
        100,
        100,
        false
    )
    private val coin = Bitmap.createScaledBitmap(
        getBitmap(context, R.drawable.ic_coin),
        75,
        75,
        false
    )

    private var viewWidth = 0F
    private var viewHeight = 0F

    private val paint = Paint().apply {
        isAntiAlias = true
    }
    private val paintBird = Paint()
    private val paintBirdCopy = Paint().apply {
        isAntiAlias = true
        color = Color.CYAN
    }
    private val paintTree = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
    }

    private var left1: Float = 0F
    private var left2: Float = 0F
    private var left3: Float = 0F
    private var gap1: Float = 0F
    private var gap2: Float = 0F
    private var gap3: Float = 0F
    private var gapChanged1 = false
    private var gapChanged2 = false
    private var gapChanged3 = false
    private var coinLeft1 = 0F
    private var coinLeft2 = 0F
    private var coinLeft3 = 0F
    private var coinRandom = 0F
    private var coinTop1 = 0F
    private var coinTop2 = 0F
    private var coinTop3 = 0F
    private val treeSpace: Float = 0.8F
    private var birdTop: Float = 0F
    private var birdLeft: Float = 0F
    private var hit = false

    private var birdUpAnimator: ValueAnimator? = null
    private lateinit var birdFallAnimator: ValueAnimator

    init {
        prepare()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawTrees(canvas)

        drawBird(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = when (event?.action) {
        MotionEvent.ACTION_DOWN -> {
            if (!hit) {
                performClick()
            }
            true
        }
        else -> false
    }

    override fun performClick(): Boolean {
        super.performClick()
        startFlyAnimation()
        return true
    }

    private fun prepare() {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Get View Size
                viewHeight = height.toFloat()
                viewWidth = width.toFloat()

                // Bird start position
                birdTop = (viewHeight / 2) - (bird.height / 2)
                birdLeft = (viewWidth / 2) - (bird.width / 2)

                coinTop1 = Random.nextFloat() * viewHeight
                coinTop2 = Random.nextFloat() * viewHeight
                coinTop3 = Random.nextFloat() * viewHeight

                moveTrees()

                // Create animator for bird to fall until user clicks
                birdFallAnimator = createBirdFallAnimator()
                birdFallAnimator.start()
            }
        })
    }

    private fun drawTrees(canvas: Canvas?) {
        canvas?.drawRect(
            RectF(left1, viewHeight - tree.height + gap1, left1 + tree.width, viewHeight),
            paintTree
        )
        canvas?.drawRect(
            RectF(left2, viewHeight - tree.height + gap2, left2 + tree.width, viewHeight),
            paintTree
        )
        canvas?.drawRect(
            RectF(left3, viewHeight - tree.height + gap3, left3 + tree.width, viewHeight),
            paintTree
        )

        canvas?.drawRect(RectF(left1, 0F, left1 + tree.width, 0F + tree.height + gap1), paintTree)
        canvas?.drawRect(RectF(left2, 0F, left2 + tree.width, 0F + tree.height + gap2), paintTree)
        canvas?.drawRect(RectF(left3, 0F, left3 + tree.width, 0F + tree.height + gap3), paintTree)

        /*canvas?.drawBitmap(tree, left1, viewHeight - tree.height, paint)
        canvas?.drawBitmap(reversedTree, left1, 0F, paint)

        canvas?.drawBitmap(tree, left2, viewHeight - tree.height, paint)
        canvas?.drawBitmap(reversedTree, left2, 0F, paint)

        canvas?.drawBitmap(tree, left3, viewHeight - tree.height, paint)
        canvas?.drawBitmap(reversedTree, left3, 0F, paint)*/

        canvas?.drawBitmap(coin, coinLeft1, coinTop1, paint)
        canvas?.drawBitmap(coin, coinLeft2, coinTop2, paint)
        canvas?.drawBitmap(coin, coinLeft3, coinTop3, paint)
    }

    private fun drawBird(canvas: Canvas?) {
        /*canvas?.drawBitmap(
            bird,
            (viewWidth / 2) - (bird.width / 2),
            birdTop,
            paintBird
        )*/
        canvas?.drawRect(
            Rect(
                birdLeft.toInt(),
                birdTop.toInt(),
                (birdLeft + bird.width).toInt(),
                (birdTop + bird.height).toInt()
            ), paintBirdCopy
        )
    }

    private fun moveTrees() {
        ValueAnimator.ofFloat(0F, -treeSpace, -2 * treeSpace, -3 * treeSpace).run {
            duration = 5000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { it ->
                val value = it.animatedValue as Float

                if (isHit()) {
                    birdUpAnimator?.cancel()
                    cancel()
                } else {
                    changeTreesCoordinateX(value)
                    invalidate()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator?) {
                    gapChanged1 = false
                    gapChanged2 = false
                    gapChanged3 = false
                }
            })
            start()
        }
    }

    private fun changeTreesCoordinateX(value: Float) {
        if (value <= 0 && value > -treeSpace) {
            if (!gapChanged3) {
                gap3 = Random.nextDouble(-200.0, 200.0).toFloat()
//                coinTop2 = Random.nextFloat() * viewHeight - coin.height
                gapChanged3 = true
            }

            left1 = viewWidth * value
            left2 = viewWidth * (value + treeSpace)
            left3 = viewWidth * (value + 2 * treeSpace)
        } else if (value <= -treeSpace && value > -2 * treeSpace) {
            if (!gapChanged1) {
                gap1 = Random.nextDouble(-200.0, 200.0).toFloat()
//                coinTop3 = Random.nextFloat() * viewHeight - coin.height
                gapChanged1 = true
            }

            left1 = viewWidth * (value + 3 * treeSpace)
            left2 = viewWidth * (value + treeSpace)
            left3 = viewWidth * (value + 2 * treeSpace)
        } else if (value <= -2 * treeSpace && value > -3 * treeSpace) {
            if (!gapChanged2) {
                gap2 = Random.nextDouble(-200.0, 200.0).toFloat()
//                coinTop1 = Random.nextFloat() * viewHeight - coin.height
                gapChanged2 = true
            }

            left1 = viewWidth * (value + 3 * treeSpace)
            left2 = viewWidth * (value + 4 * treeSpace)
            left3 = viewWidth * (value + 2 * treeSpace)
        }
    }

    private fun startFlyAnimation() {
        // Cancel previous animations
        birdFallAnimator.cancel()
        birdUpAnimator?.cancel()

        // Start Fly
        birdUpAnimator = createBirdUpAnimator()
        birdUpAnimator?.start()
    }

    private fun createBirdFallAnimator(): ValueAnimator =
        ValueAnimator.ofFloat(birdTop, viewHeight).apply {
            duration = 800
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                birdTop = if (value <= viewHeight - bird.height) {
                    value
                } else {
                    viewHeight - bird.height
                }
                invalidate()
            }
        }

    private fun createBirdUpAnimator(): ValueAnimator =
        ValueAnimator.ofFloat(birdTop, birdTop - 100).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                if (!hit) {
                    val value = it.animatedValue as Float
                    birdTop = if (value >= 0) {
                        value
                    } else {
                        0F
                    }
                    invalidate()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    birdFallAnimator.cancel()
                    birdFallAnimator = createBirdFallAnimator()
                    birdFallAnimator.start()
                }
            })
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

    private fun isHit(): Boolean {
        if (((isTreeXInRange(left1, left1 + tree.width))
                    && (isTreeYInRange(viewHeight - tree.height + gap1, viewHeight)))
            || ((isTreeXInRange(left2, left2 + tree.width))
                    && (isTreeYInRange(viewHeight - tree.height + gap2, viewHeight)))
            || ((isTreeXInRange(left3, left3 + tree.width))
                    && (isTreeYInRange(viewHeight - tree.height + gap3, viewHeight)))
            || ((isTreeXInRange(left1, left1 + tree.width))
                    && (isTreeYInRange(0F, 0F + tree.height + gap1)))
            || ((isTreeXInRange(left2, left2 + tree.width))
                    && (isTreeYInRange(0F, 0F + tree.height + gap2)))
            || ((isTreeXInRange(left3, left3 + tree.width))
                    && (isTreeYInRange(0F, 0F + tree.height + gap3)))
        ) {
            hit = true
            return true
        }
        return false
    }

    private fun isTreeXInRange(leftX: Float, rightX: Float): Boolean {
        return (birdLeft in leftX..rightX) || (birdLeft + bird.width in leftX..rightX)
    }

    private fun isTreeYInRange(topY: Float, bottomY: Float): Boolean {
        return (birdTop in topY..bottomY) || ((birdTop + bird.height) in topY..bottomY)
    }

    private fun getBitmap(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)!!
        drawable = DrawableCompat.wrap(drawable).mutate()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}