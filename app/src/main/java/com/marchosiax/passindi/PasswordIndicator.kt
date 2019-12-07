package com.marchosiax.passindi

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.max
import kotlin.math.min

class PasswordIndicator(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val balls = ArrayList<Ball>()

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var ringColor = Color.BLACK
    private var fillColor = Color.BLACK

    private var ballsCount = 3

    private var ballRadius = 8f.dp(context)
    private var ballMargin = 4f.dp(context)
    private var fillMargin = 2f.dp(context)
    private var ringStroke = 1f.dp(context)

    private val animDuration = 200L
    private val animInterpolator = AccelerateDecelerateInterpolator()

    init {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let { getAttributes(it) }

        ringPaint.apply {
            color = ringColor
            strokeWidth = ringStroke
            style = Paint.Style.STROKE
        }

        fillPaint.apply {
            color = fillColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)
        measureBalls()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        balls.forEach {
            canvas?.drawCircle(it.x, it.y, it.radius, ringPaint)
            if (it.fill)
                canvas?.drawCircle(it.x, it.y, it.fillRadius, fillPaint)
        }
    }

    private fun getAttributes(attrs: AttributeSet) {
        with(context.obtainStyledAttributes(attrs, R.styleable.PasswordIndicator, 0, 0)) {
            ballsCount = getInteger(R.styleable.PasswordIndicator_count, ballsCount)
            ballRadius = getDimension(R.styleable.PasswordIndicator_radius, ballRadius)
            ballMargin = getDimension(R.styleable.PasswordIndicator_ringMargin, ballMargin)
            fillMargin = getDimension(R.styleable.PasswordIndicator_fillMargin, fillMargin)
            ringStroke = getDimension(R.styleable.PasswordIndicator_ringStroke, ringStroke)
            ringColor = getColor(R.styleable.PasswordIndicator_ringColor, ringColor)
            fillColor = getColor(R.styleable.PasswordIndicator_fillColor, fillColor)
        }
    }

    private fun measureWidth(widthSpec: Int): Int {
        val availableWidth = MeasureSpec.getSize(widthSpec)
        val mode = MeasureSpec.getMode(widthSpec)

        val padding = paddingEnd + paddingStart
        val size = ((ballsCount * (ballMargin - 1)) + ballsCount * ballRadius * 2) + padding

        return when (mode) {
            MeasureSpec.EXACTLY -> availableWidth
            MeasureSpec.UNSPECIFIED -> size.toInt()
            else -> min(availableWidth, size.toInt())
        }
    }

    private fun measureHeight(heightSpec: Int): Int {
        val availableHeight = MeasureSpec.getSize(heightSpec)
        val mode = MeasureSpec.getMode(heightSpec)

        val size = paddingTop + paddingBottom + ballRadius * 2

        return when (mode) {
            MeasureSpec.EXACTLY -> availableHeight
            MeasureSpec.UNSPECIFIED -> size.toInt()
            else -> min(availableHeight, size.toInt())
        }
    }

    private fun measureBalls() {
        var calculatedX = 0f + max(paddingStart, paddingLeft)

        balls.clear()
        repeat(ballsCount) {
            balls.add(
                Ball(
                    x = calculatedX + ballRadius,
                    y = (measuredHeight / 2 + paddingTop - paddingBottom).toFloat(),
                    radius = ballRadius,
                    holeColor = ringPaint,
                    circleColor = fillPaint
                )
            )

            val margin: Float = if (it != ballsCount - 1) ballMargin else 0f
            calculatedX += ballRadius * 2 + margin
        }
    }

    private fun getFillRadius() = ballRadius - fillMargin

    private fun animateFill(ball: Ball) {
        ValueAnimator.ofFloat(0f, getFillRadius()).apply {
            duration = animDuration
            interpolator = animInterpolator
            onAnimationEnd { ball.fillRadius = getFillRadius() }
            addUpdateListener {
                ball.fillRadius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun animateEmpty(ball: Ball) {
        ValueAnimator.ofFloat(getFillRadius(), 0f).apply {
            duration = animDuration
            interpolator = animInterpolator
            onAnimationEnd { ball.fillRadius = 0f }
            addUpdateListener {
                ball.fillRadius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun fillNext() {
        var targetBall: Ball? = null
        var changeHappened = false

        for (i in 0 until ballsCount) {
            targetBall = balls[i]
            if (!targetBall.fill) {
                targetBall.fill = true
                changeHappened = true
                break
            }
        }

        if (targetBall != null && changeHappened)
            animateFill(targetBall)
    }

    fun emptyCurrent() {
        var targetBall: Ball? = null
        var changeHappened = false

        for (i in 0 until ballsCount) {
            val firstEmptyBall = balls[i]
            if (!firstEmptyBall.fill && i > 0) {
                targetBall = balls[i - 1]
                targetBall.fill = false
                changeHappened = true
                break
            }

            if (i == ballsCount - 1) {
                targetBall = balls[i]
                targetBall.fill = false
                changeHappened = true
                break
            }
        }

        if (targetBall != null && changeHappened)
            animateEmpty(targetBall)
    }

    fun clear() {
        balls.forEach {
            it.fill = false
            animateEmpty(it)
        }
    }

    private data class Ball(
        var x: Float,
        var y: Float,
        var radius: Float,
        var holeColor: Paint,
        var circleColor: Paint,
        var fillRadius: Float = 0f,
        var fill: Boolean = false
    )

}