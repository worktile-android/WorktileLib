package com.worktile.ui.widgets;
//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.UiThread;


/**
 * Created by Android Studio.
 * User: guolei
 * Email: guolei@worktile.com
 * Date: 17/8/4
 * Time: 下午3:08
 * Desc:
 */
public class HorizontalProgressBar extends View {

    private static final boolean DEFAULT_ALLOW_ANIM = true;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#f1f1f1");
    private static final int DEFAULT_PROGRESS_COLOR = Color.parseColor("#348fe4");
    private static final int DEFAULT_RADIUS = Integer.MAX_VALUE;

    private boolean mAllowAnim;
    private int mBackgroundColor;
    private int mProgressColor;
    private int mRadius;

    private int mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom;
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;

    private int mWidth, mHeight;
    private RectF mBackgroundRectF;
    private RectF mProgressRectF;

    private ValueAnimator animator;
    private float mLastValue;

    public HorizontalProgressBar(Context context) {
        this(context, null);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setAntiAlias(true);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBar);
        mBackgroundColor = typedArray.getColor(R.styleable.HorizontalProgressBar_hp_backgroundColor,
                DEFAULT_BACKGROUND_COLOR);
        mProgressColor = typedArray.getColor(R.styleable.HorizontalProgressBar_hp_progressColor,
                DEFAULT_PROGRESS_COLOR);
        mAllowAnim = typedArray.getBoolean(R.styleable.HorizontalProgressBar_hp_allowAnim,
                DEFAULT_ALLOW_ANIM);
        mRadius = typedArray.getDimensionPixelSize(R.styleable.HorizontalProgressBar_hp_circular,
                DEFAULT_RADIUS);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();
        initRectF();
    }

    private void initRectF() {
        mBackgroundRectF = new RectF(mPaddingLeft, mPaddingTop,
                mWidth - mPaddingRight, mHeight - mPaddingBottom);
        if (mProgressRectF == null) {
            mProgressRectF = new RectF(mPaddingLeft, mPaddingTop, 0, mHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(mBackgroundRectF, mRadius, mRadius, mBackgroundPaint);
        canvas.drawRoundRect(mProgressRectF, mRadius, mRadius, mProgressPaint);
    }

    @UiThread
    public void startAnim(@FloatRange(from = .0, to = 1.0) float endValue) {
        if (endValue == mLastValue) {
            return;
        }
        mLastValue = endValue;
        if (!mAllowAnim) return;
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0, endValue);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float f = animation.getAnimatedFraction();
            if (mProgressRectF == null) {
                mProgressRectF = new RectF(mPaddingLeft, mPaddingTop, 0, mHeight);
            }
            mProgressRectF.set(mPaddingLeft, mPaddingTop,
                    (mWidth - mPaddingRight) * f * endValue, mHeight - mPaddingBottom);
            invalidate();
        });
        animator.setDuration(300).start();
    }

    public boolean isAllowAnim() {
        return mAllowAnim;
    }

    public void setAllowAnim(boolean mAllowAnim) {
        this.mAllowAnim = mAllowAnim;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
        invalidate();
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int mProgressColor) {
        this.mProgressColor = mProgressColor;
        mProgressPaint.setColor(mProgressColor);
        invalidate();
    }

    public void setProgress(double progress) {
        startAnim((float) progress);
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
        invalidate();
    }
}
