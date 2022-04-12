package com.worktile.ui.widgets

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.worktile.common.utils.UnitConversion.dp2px

/**
 * @Created by zhangqian on 2017/8/21.
 */
class WorktileProgressBar(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    /**
     * 小球
     */
    inner class Ball {
        var centerX //圆心位置
                = 0f
        var color //颜色
                = 0
    }

    //画笔
    private var mPaint: Paint? = null
    private var mBall1: Ball? = null
    private var mBall2: Ball? = null
    private var mBall3: Ball? = null
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var animatorSet: AnimatorSet? = null

    constructor(context: Context?) : this(context, null) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
        init(context)
    }

    private fun init(context: Context?) {
        mBall1 = Ball()
        mBall2 = Ball()
        mBall3 = Ball()
        RADIUS = dp2px(context!!, 5f)
        DISTANCE = dp2px(context, 5f) * 3
        mBall1!!.color = COLOR_BALL_LEFT
        mBall2!!.color = COLOR_BALL_CENTER
        mBall3!!.color = COLOR_BALL_RIGHT
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        configAnimator()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = w / 2.toFloat()
        mCenterY = h / 2.toFloat()
        mBall2!!.centerX = mCenterX
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mCenterX = width / 2.toFloat()
        mCenterY = height / 2.toFloat()
        mBall2!!.centerX = mCenterX
    }

    override fun onDraw(canvas: Canvas) {
        mPaint!!.color = mBall1!!.color
        canvas.drawCircle(mBall1!!.centerX, mCenterY, RADIUS.toFloat(), mPaint!!)
        mPaint!!.color = mBall2!!.color
        canvas.drawCircle(mBall2!!.centerX, mCenterY, RADIUS.toFloat(), mPaint!!)
        mPaint!!.color = mBall3!!.color
        canvas.drawCircle(mBall3!!.centerX, mCenterY, RADIUS.toFloat(), mPaint!!)
    }

    /**
     * 配置属性动画
     */
    private fun configAnimator() {

        //第一个小球位移动画，通过改变小球的圆心
        val leftAnimator = ValueAnimator.ofFloat(-1f, 0f, 1f, 0f, -1f)
        leftAnimator.repeatCount = ValueAnimator.INFINITE
        leftAnimator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Float
            val x = mCenterX + DISTANCE * value
            mBall1!!.centerX = x
            //不停的刷新view，让view不停的重绘
            invalidate()
        }

        //第二个小球位移动画
        val rightAnimator = ValueAnimator.ofFloat(1f, 0f, -1f, 0f, 1f)
        rightAnimator.repeatCount = ValueAnimator.INFINITE
        rightAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val x = mCenterX + DISTANCE * value
            mBall3!!.centerX = x
        }

        //属性动画集合
        animatorSet = AnimatorSet()
        animatorSet!!.playTogether(leftAnimator, rightAnimator)
        //动画一次运行时间
        animatorSet!!.interpolator = LinearInterpolator()
        animatorSet!!.duration = DURATION.toLong()
    }

    override fun setVisibility(v: Int) {
        if (visibility != v) {
            super.setVisibility(v)
            if (v == GONE || v == INVISIBLE) {
                stopAnimator()
            } else {
                startAnimator()
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, v: Int) {
        super.onVisibilityChanged(changedView, v)
        if (v == GONE || v == INVISIBLE) {
            stopAnimator()
        } else {
            startAnimator()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimator()
    }

    /**
     * 开始动画
     */
    fun startAnimator() {
        if (visibility != VISIBLE) return
        if (animatorSet!!.isRunning) return
        if (animatorSet != null) {
            animatorSet!!.start()
        }
    }

    /**
     * 结束停止动画
     */
    fun stopAnimator() {
        if (animatorSet != null) {
            animatorSet!!.end()
        }
    }

    companion object {
        private var RADIUS = 0
        private var DISTANCE = 0
        private val COLOR_BALL_RIGHT = Color.parseColor("#66348fe4")
        private val COLOR_BALL_CENTER = Color.parseColor("#348fe4")
        private val COLOR_BALL_LEFT = Color.parseColor("#cc348fe4")
        private const val DURATION = 1000
    }

    init {
        init(context)
    }
}