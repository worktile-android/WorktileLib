package com.worktile.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.FloatRange
import com.worktile.common.utils.UnitConversion.dp2px
import java.math.BigDecimal
import java.math.RoundingMode

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|
/**
 * Created by Android Studio.
 * User: guolei
 * Email: guolei@worktile.com
 * Date: 17/8/15
 * Time: 上午10:04
 * Desc:
 */
class CircleProgressWithText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mCurtail = 0
    private var mCircleBorderWidth = 0
    private var mCircleBackgroundColor = 0
    private var mCircleProgressColor = 0
    private var mTextColor = 0
    private var mTextSize = 0
    private var mCircleBackgroundPaint: Paint? = null
    private var mCircleProgressPaint: Paint? = null
    private var mTextPaint: Paint? = null
    private var mViewSize = 0
    private var mRectF: RectF? = null
    private var mCenterX = 0
    private var mCenterY = 0
    private var mRadius = 0
    private var mTextBaseLineY = 0
    private var mTextBaseLineX = 0
    private var mProgress = 0f
    private var mSweepAngle = 0
    private var mProgressStr = "0%"
    private var mLastValue = 0f
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressWithText)
        mCircleBackgroundColor = typedArray.getColor(
            R.styleable.CircleProgressWithText_cp_backgroundColor,
            DEFAULT_BACKGROUND_COLOR
        )
        mCircleProgressColor = typedArray.getColor(
            R.styleable.CircleProgressWithText_cp_progressColor,
            DEFAULT_PROGRESS_COLOR
        )
        mTextColor = typedArray.getColor(
            R.styleable.CircleProgressWithText_cp_textColor,
            mCircleProgressColor
        )
        mTextSize = typedArray.getDimensionPixelSize(
            R.styleable.CircleProgressWithText_cp_textSize,
            dp2px(context, DEFAULT_TEXT_SIZE.toFloat())
        )
        mCircleBorderWidth = dp2px(context, DEFAULT_CIRCLE_BORDER_DP.toFloat())
        mCurtail = mCircleBorderWidth / 2
        typedArray.recycle()
    }

    private fun initPaint() {
        mCircleBackgroundPaint = Paint()
        mCircleBackgroundPaint!!.style = Paint.Style.STROKE
        mCircleBackgroundPaint!!.isAntiAlias = true
        mCircleBackgroundPaint!!.color = mCircleBackgroundColor
        mCircleBackgroundPaint!!.strokeWidth = mCircleBorderWidth.toFloat()
        mCircleProgressPaint = Paint()
        mCircleProgressPaint!!.style = Paint.Style.STROKE
        mCircleProgressPaint!!.isAntiAlias = true
        mCircleProgressPaint!!.color = mCircleProgressColor
        mCircleProgressPaint!!.strokeWidth = mCircleBorderWidth.toFloat()
        mCircleProgressPaint!!.strokeCap = Paint.Cap.ROUND
        mTextPaint = Paint()
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.color = mTextColor
        mTextPaint!!.textSize = dp2px(context, 14f).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewSize =
            Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(mViewSize, mViewSize)
        initFixedLocation()
    }

    private fun initFixedLocation() {
        if (mRectF == null) {
            mRectF = RectF(
                mCurtail.toFloat(),
                mCurtail.toFloat(),
                (mViewSize - mCurtail).toFloat(),
                (mViewSize - mCurtail).toFloat()
            )
            // 需要加上 top的 1/2 空白
            val fontMetrics = mTextPaint!!.fontMetrics
            mTextBaseLineY =
                (mViewSize / 2 + mTextSize / 2 - (fontMetrics.ascent - fontMetrics.top) / 2).toInt()
            mCenterY = mViewSize / 2
            mCenterX = mCenterY
            mRadius = mCenterX - mCurtail
            mTextBaseLineX = (mViewSize - mTextPaint!!.measureText(
                mProgressStr,
                0,
                mProgressStr.length
            )).toInt() / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(
            mCenterX.toFloat(),
            mCenterY.toFloat(),
            mRadius.toFloat(),
            mCircleBackgroundPaint!!
        )
        canvas.drawArc(mRectF!!, -90f, mSweepAngle.toFloat(), false, mCircleProgressPaint!!)
        canvas.drawText(
            mProgressStr,
            mTextBaseLineX.toFloat(),
            mTextBaseLineY.toFloat(),
            mTextPaint!!
        )
    }

    fun startAnim(@FloatRange(from = .0, to = 1.0) endValue: Float) {
        if (mLastValue == endValue) {
            return
        }
        mLastValue = endValue
        val animator = ValueAnimator.ofFloat(0f, 1f)
            .setDuration(300)
        animator.addUpdateListener { animation: ValueAnimator ->
            mProgress = animation.animatedFraction * endValue
            mProgressStr = BigDecimal(mProgress.toDouble() * 100).setScale(0, RoundingMode.DOWN).toString()
            mProgressStr = "$mProgressStr%"
            mSweepAngle = (360 * mProgress).toInt()
            mTextBaseLineX = (mViewSize - mTextPaint!!.measureText(
                mProgressStr,
                0,
                mProgressStr.length
            )).toInt() / 2
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    fun setProgress(progress: Double) {
        startAnim(progress.toFloat())
    }

    fun setProgressColor(color: Int) {
        mCircleProgressPaint!!.color = color
        mTextPaint!!.color = color
        invalidate()
    }

    companion object {
        private const val DEFAULT_CIRCLE_BORDER_DP = 5
        private const val DEFAULT_TEXT_SIZE = 14
        private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#f1f1f1")
        private val DEFAULT_PROGRESS_COLOR = Color.parseColor("#348fe4")
    }

    init {
        initAttrs(context, attrs)
        initPaint()
    }
}