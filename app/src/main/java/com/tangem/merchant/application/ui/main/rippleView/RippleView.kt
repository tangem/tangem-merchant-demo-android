package com.tangem.merchant.application.ui.main.rippleView

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ListView
import android.widget.RelativeLayout
import com.tangem.merchant.R

/**
 * Created by Anton Zhilenkov on 31.08.17.
 */
class RippleView : RelativeLayout {

    private var WIDTH: Int = 0
    private var HEIGHT: Int = 0
    private var FRAME_RATE = 10
    private var DURATION = 400
    private var PAINT_ALPHA = 90

    private var gestureDetector: GestureDetector? = null
    private var scaleAnimation: ScaleAnimation? = null
    private var canvasHandler: Handler? = null
    private lateinit var paint: Paint
    private var mAnimationListener: RippleAnimationListener? = null
    private var originBitmap: Bitmap? = null

    private var animationRunning = false
    private var hasToZoom = false
    private var isCentered = false
    private var radiusMax = 0f
    private var mX = -1f
    private var mY = -1f
    private var zoomScale: Float = 0f
    private var zoomDuration: Int = 0
    private var rippleColor: Int = 0
    private var ripplePadding: Int = 0
    private var rippleType: Int? = null
    private var timer = 0
    private var timerEmpty = 0
    private var durationEmpty = -1
    private val runnable = Runnable { invalidate() }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        if (isInEditMode) return

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView)
        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, resources.getColor(R.color.rippleColor))
        rippleType = typedArray.getInt(R.styleable.RippleView_rv_type, 0)
        hasToZoom = typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false)
        isCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false)
        DURATION = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, DURATION)
        FRAME_RATE = typedArray.getInteger(R.styleable.RippleView_rv_framerate, FRAME_RATE)
        PAINT_ALPHA = typedArray.getInteger(R.styleable.RippleView_rv_alpha, PAINT_ALPHA)
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0)
        canvasHandler = Handler()
        zoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, 1.03f)
        zoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, 200)

        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = rippleColor
        paint.alpha = PAINT_ALPHA

        this.setWillNotDraw(false)

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(event: MotionEvent) {
                super.onLongPress(event)
                animateRipple(event)
                sendClickEvent(true)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return true
            }
        })

        this.isDrawingCacheEnabled = true
        this.isClickable = true
    }

    fun animateRipple(event: MotionEvent) {
        createAnimation(event.x, event.y)
    }

    fun animateRipple(x: Float, y: Float) {
        createAnimation(x, y)
    }

    private fun createAnimation(x: Float, y: Float) {
        animationRunning = false
        timer = 0
        durationEmpty = -1
        timerEmpty = 0
        this.clearAnimation()

        if (hasToZoom) {
            this.startAnimation(scaleAnimation)
        }

        radiusMax = Math.max(WIDTH, HEIGHT).toFloat()

        if (rippleType != 2) radiusMax /= 2f

        radiusMax -= ripplePadding.toFloat()

        if (isCentered || rippleType == 1) {
            this.mX = (measuredWidth / 2).toFloat()
            this.mY = (measuredHeight / 2).toFloat()
        } else {
            this.mX = x
            this.mY = y
        }

        animationRunning = true
        if (rippleType == 1 && originBitmap == null) originBitmap = getDrawingCache(true)

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector!!.onTouchEvent(event)) {
            animateRipple(event)
            sendClickEvent(false)
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        WIDTH = w
        HEIGHT = h

        scaleAnimation = ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, (w / 2).toFloat(), (h / 2).toFloat())
        scaleAnimation!!.duration = zoomDuration.toLong()
        scaleAnimation!!.repeatMode = Animation.REVERSE
        scaleAnimation!!.repeatCount = 1
    }

    override fun draw(canvas: Canvas) {
        try {
            super.draw(canvas)
            if (animationRunning) {
                if (DURATION <= timer * FRAME_RATE) {
                    if (mAnimationListener != null) {
                        mAnimationListener!!.onRippleAnimationEnd()
                    }
                    animationRunning = false
                    timer = 0
                    durationEmpty = -1
                    timerEmpty = 0
                    canvas.restore()
                    invalidate()
                    return
                } else {
                    canvasHandler!!.postDelayed(runnable, FRAME_RATE.toLong())
                }

                if (timer == 0) canvas.save()

                canvas.drawCircle(mX, mY, radiusMax * (timer.toFloat() * FRAME_RATE / DURATION), paint!!)

                paint.color = resources.getColor(android.R.color.holo_red_light)

                if (rippleType == 1 && originBitmap != null && timer.toFloat() * FRAME_RATE / DURATION > 0.4f) {
                    if (durationEmpty == -1) durationEmpty = DURATION - timer * FRAME_RATE

                    timerEmpty++
                    val tmpBitmap =
                        getCircleBitmap((radiusMax * (timerEmpty.toFloat() * FRAME_RATE / durationEmpty)).toInt())
                    canvas.drawBitmap(tmpBitmap, 0f, 0f, paint)
                    tmpBitmap.recycle()
                }

                paint.color = rippleColor

                if (rippleType == 1) {
                    if (timer.toFloat() * FRAME_RATE / DURATION > 0.6f) {
                        paint.alpha =
                            (PAINT_ALPHA - PAINT_ALPHA * (timerEmpty.toFloat() * FRAME_RATE / durationEmpty)).toInt()
                    } else {
                        paint.alpha = PAINT_ALPHA
                    }
                } else {
                    paint.alpha = (PAINT_ALPHA - PAINT_ALPHA * (timer.toFloat() * FRAME_RATE / DURATION)).toInt()
                }

                timer++
            }
        } catch (e: RuntimeException) {
            if (mAnimationListener != null) {
                mAnimationListener!!.onRippleAnimationEnd()
            }
        }

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        this.onTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    private fun sendClickEvent(isLongClick: Boolean?) {
        if (parent is ListView) {
            val position = (parent as ListView).getPositionForView(this)
            val id = (parent as ListView).getItemIdAtPosition(position)
            if (isLongClick!!) {
                if ((parent as ListView).onItemLongClickListener != null) {
                    (parent as ListView).onItemLongClickListener.onItemLongClick(parent as ListView, this, position, id)
                }
            } else {
                if ((parent as ListView).onItemClickListener != null) {
                    (parent as ListView).onItemClickListener!!.onItemClick(parent as ListView, this, position, id)
                }
            }
        }
    }

    private fun getCircleBitmap(radius: Int): Bitmap {
        val output = Bitmap.createBitmap(originBitmap!!.width, originBitmap!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect((mX - radius).toInt(), (mY - radius).toInt(), (mX + radius).toInt(), (mY + radius).toInt())

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(mX, mY, radius.toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(originBitmap!!, rect, rect, paint)

        return output
    }

    fun setRippleAnimationListener(rippleAnimationListener: RippleAnimationListener) {
        this.mAnimationListener = rippleAnimationListener
    }
}
