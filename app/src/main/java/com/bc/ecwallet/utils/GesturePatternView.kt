package com.bc.ecwallet.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.bc.ecwallet.R

class GesturePatternView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Float.dp: Float get() = (this / Resources.getSystem().displayMetrics.density)
    val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Float.px: Float get() = (this * Resources.getSystem().displayMetrics.density)

    val listGesture = arrayListOf<Int>()
    var onGestureAddedCallback:(()->Unit)? = null

    public var gapSize = 8.px
    public var itemHeight = 200.px
    public var itemWidth = 100.px
    public var itemNumber = 4

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        var height: Int

        val expectedWidth = itemWidth*itemNumber + gapSize*(itemNumber+1)
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width = expectedWidth
        }


        if(expectedWidth>widthSize){
            width = widthSize
            itemWidth = (widthSize - gapSize*(itemNumber+1))/itemNumber
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height = itemHeight + gapSize*2
        }

        setMeasuredDimension(Math.min(width, widthSize), Math.min(height, heightSize))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paintRectangle = Paint()
        paintRectangle.style = Paint.Style.FILL
        paintRectangle.color = Color.WHITE
        paintRectangle.setShadowLayer(2f.px,0f,0f,Color.GRAY)
        this.setLayerType(LAYER_TYPE_SOFTWARE, paintRectangle);

        var x = gapSize.toFloat()
        for(i in 0 until itemNumber){
            canvas?.drawRoundRect(x, gapSize.toFloat(), x+itemWidth,(gapSize+itemHeight).toFloat(),40f,40f,paintRectangle)
            x += itemWidth + gapSize
        }

        for(i in 0 until listGesture.size){
            val gesture = listGesture[i]

            var icon: Drawable = resources.getDrawable(R.drawable.ic_left,null)

            when(gesture){
                0->{
                    // left
                    icon = resources.getDrawable(R.drawable.ic_left,null)
                }
                1->{
                    // right
                    icon = resources.getDrawable(R.drawable.ic_right,null)
                }
                2->{
                    // down
                    icon = resources.getDrawable(R.drawable.ic_down,null)
                }
                3->{
                    // up
                    icon = resources.getDrawable(R.drawable.ic_up,null)
                }
            }


            val left = i*itemWidth + (i+2)*gapSize
            val right = left+itemWidth-gapSize*2
            icon.setBounds(left,height/2-(right-left)/2,right,height/2+(right-left)/2)
            icon.draw(canvas!!)
            invalidate()
        }
    }

    fun addGesture(gesture:Int){
        if(listGesture.size<itemNumber){
            listGesture.add(gesture)
            invalidate()
        }
    }

    fun clear(){
        listGesture.removeAll(listGesture)
        invalidate()
    }
}