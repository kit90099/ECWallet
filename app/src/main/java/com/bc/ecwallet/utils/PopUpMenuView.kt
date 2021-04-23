package com.bc.ecwallet.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import com.bc.ecwallet.R

class PopUpMenuView : View {
    val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Float.dp: Float get() = (this / Resources.getSystem().displayMetrics.density)
    val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Float.px: Float get() = (this * Resources.getSystem().displayMetrics.density)

    private var anchor: Int = 0
    private var anchorX: Float = -1.0f
    private var anchorY: Float = -1.0f
    private val lenTriangle = 30f.px
    private val angleTriangle = 90.0
    private val menuItemHeight = 40f.px
    private val menuGap = 8f.px
    private val widthRetc = 200f.px
    private val internalPadding = 4f.px

    private var menuItems = arrayListOf<MenuItem>()

    private var onItemClickedCallback: ((String) -> Unit)? = null

    public var textColor: Int = Color.parseColor("#AA000000")
    public var textSize: Int = 20.px

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(attributeSet)
    }

    constructor(context: Context?, attributeSet: AttributeSet, defStypedAttr: Int) : super(
        context,
        attributeSet,
        defStypedAttr
    ) {
        init(attributeSet)
    }

    private fun init(attributeSet: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.PopupMenuView)
        val n = ta.indexCount

        for (i in 0..n - 1) {
            val attr = ta.getIndex(i)
            when (attr) {
                R.styleable.PopupMenuView_anchor -> {
                    anchor = ta.getResourceId(R.styleable.PopupMenuView_anchor, 0)
                }
                R.styleable.PopupMenuView_textColor -> {
                    textColor = ta.getColor(
                        R.styleable.PopupMenuView_textColor,
                        Color.parseColor("#AA000000")
                    )
                }
                R.styleable.PopupMenuView_textSize -> {
                    textSize =
                        ta.getDimensionPixelSize(R.styleable.PopupMenuView_textSize, textColor)
                }
            }
        }

        visibility = GONE
    }

    fun setMenu(menu: ArrayList<MenuItem>) {
        menuItems = menu
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        var height: Int

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width = (widthRetc + internalPadding * 2).toInt()
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height = (lenTriangle * Math.cos(Math.toRadians(angleTriangle / 2)) +
                    menuGap * (menuItems.size + 1) +
                    menuItemHeight * menuItems.size +
                    internalPadding * 2
                    ).toInt()
        }

        setMeasuredDimension(Math.min(width, widthSize), Math.min(height, heightSize))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        if (anchor != 0) {
            val anchor = rootView.findViewById<View>(anchor)
            anchorX = anchor.x + anchor.width / 2
            anchorY = anchor.y

            this.x = (anchor.x + (anchor.width / 2) - width / 2)
            this.y = (anchor.y - height)
        }

        if (anchorX >= 0 && anchorY >= 0 && menuItems.size != 0) {
            // canvas?.drawARGB(10, 255, 0, 0)

            val paintContainer = Paint()
            paintContainer.color = Color.parseColor("#ffffff")
            val radius = 30.0f
            val corEffect = CornerPathEffect(radius)
            paintContainer.pathEffect = corEffect
            paintContainer.setShadowLayer(16f, 0f, 0f, Color.argb(100, 0, 0, 0))
            setLayerType(LAYER_TYPE_SOFTWARE, paintContainer)

            val container = Path()

            /*
            *   D                       E
            *    -------------------------
            *   |                       |
            *   |                       |
            *   |                       |
            *   |                       |
            *   -------------------------F
            *   C          B\/G
            *               A
            * */

            val a_X = (width / 2).toFloat()
            val a_Y = height.toFloat() - internalPadding
            val b_X = a_X - lenTriangle * Math.sin(Math.toRadians(angleTriangle / 2)).toFloat()
            val b_Y = a_Y - lenTriangle * Math.cos(Math.toRadians(angleTriangle / 2)).toFloat()
            val c_X = 0f + internalPadding
            val c_Y = b_Y
            val d_X = c_X
            val d_Y = 0f + internalPadding
            val e_X = width.toFloat() - internalPadding
            val e_Y = d_Y
            val f_X = e_X
            val f_Y = b_Y
            val g_X = a_X + lenTriangle * Math.sin(Math.toRadians(angleTriangle / 2)).toFloat()
            val g_Y = b_Y

            // a
            container.moveTo(a_X, a_Y)
            // b
            container.lineTo(b_X, b_Y)
            // c
            container.lineTo(c_X, c_Y)
            // d
            container.lineTo(d_X, d_Y)
            // e
            container.lineTo(e_X, e_Y)
            // f
            container.lineTo(f_X, f_Y)
            // g
            container.lineTo(g_X, g_Y)
            // original point
            container.lineTo(a_X, a_Y)
            container.close()

            canvas?.drawPath(container, paintContainer)

            for (i in 0..menuItems.size - 1) {
                val item = menuItems.get(i)
                val text = item.title
                val image = context.resources.getDrawable(item.icon, null)

                val imgLeft = c_X + (menuGap)
                val imgTop = d_Y + ((menuGap * (i + 1)) + menuItemHeight * i)
                val imgRight = (imgLeft + menuItemHeight)
                val imgBottom = imgTop + menuItemHeight

                /*val testPaint = Paint()
                testPaint.color = Color.parseColor("#ff0000")
                canvas?.drawRect(imgLeft.toFloat(),imgTop.toFloat(),imgRight.toFloat(),imgBottom.toFloat(),testPaint)*/

                image.setBounds(
                    imgLeft.toInt(),
                    imgTop.toInt(),
                    imgRight.toInt(),
                    imgBottom.toInt()
                )

                val textPaint = Paint()
                textPaint.color = textColor
                textPaint.textSize = textSize.toFloat()

                val fontMetrics = textPaint.fontMetrics
                val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom

                canvas?.drawText(
                    text,
                    imgRight + menuGap,
                    (imgBottom + imgTop) / 2 + distance,
                    textPaint
                )

                if (canvas != null) {
                    image.draw(canvas)
                }

                item.region.set(
                    imgLeft.toInt(),
                    imgTop.toInt(),
                    (e_X - menuGap).toInt(),
                    imgBottom.toInt()
                )
            }

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        for (i in 0 until menuItems.size) {
            val item = menuItems[i]
            if (item.region!!.contains(event!!.x.toInt(), event.y.toInt())) {
                onItemClickedCallback?.invoke(item.title)
            }
        }

        return super.onTouchEvent(event)
    }

    fun addOnItemTouchCallback(onItemClickedCallback: (title: String) -> Unit) {
        this.onItemClickedCallback = onItemClickedCallback
    }

    fun show() {
        if (visibility == GONE) {
            visibility = VISIBLE
        } else {
            visibility = GONE
        }
    }

    class MenuItem(var title: String, @DrawableRes var icon: Int) {
        var region: Region = Region()
    }

}