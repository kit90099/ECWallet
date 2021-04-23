package com.bc.ecwallet.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerometerListener:SensorEventListener {
    companion object{
        val thresholdX = 30f
        val thresholdY = 30f
        var ignoreValue = 150
        val X = 0
        val Y = 1
    }

    var ignorePointer = 0

    public var callback:((Int)->Unit)? = null

    private var xDetector: MotionDetector
    private var yDetector: MotionDetector

    private var onXDetectedCallback:((Boolean)->Unit)? = null
    private var onYDetectedCallback:((Boolean)->Unit)? = null

    init{
        ignorePointer = 0
        ignoreValue = 150

        xDetector = MotionDetector(thresholdX,X)

        yDetector = MotionDetector(thresholdY,Y)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    /*
    * 0: left
    * 1: right
    * 2: down
    * 3: up
    * */

    var accelerating = true
    var lastAbsValue = 0f
    var lastValue = 0f
    var increasing = false
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if(ignorePointer == 0) {

            val resX = xDetector.detectMotion(sensorEvent.values[0])
            val resY = yDetector.detectMotion(sensorEvent.values[1])

            if (resX != null) {
                callback?.invoke(
                    if (resX) {
                        1
                    } else {
                        0
                    }
                )

                ignorePointer = ignoreValue
                xDetector.reset()
                yDetector.reset()
            }

            if (resY != null) {
                callback?.invoke(
                    if (resY) {
                        3
                    } else {
                        2
                    }
                )
                ignorePointer = ignoreValue
                xDetector.reset()
                yDetector.reset()
            }
        }else{
            ignorePointer--
        }
    }



    private fun magnitude(x:Float, y:Float, z:Float):Float{
        return sqrt(x*x+y*y+z*z)
    }

    fun addOnXDetectedCallback(callback:(Boolean)->Unit){
        onXDetectedCallback = callback
    }
    fun addOnYDetectedCallback(callback:(Boolean)->Unit){
        onYDetectedCallback = callback
    }



    private class MotionDetector(val threshold:Float,val axis:Int){
        var accelerating = true
        var increasing = false
        var lastAbsValue = 0f
        var lastValue = 0f

        fun detectMotion(value:Float):Boolean?{
            val currentValue = value
            val absX = abs(currentValue)

            if(accelerating){
                if(absX >= lastAbsValue){
                    lastAbsValue = absX
                    lastValue = currentValue
                }else{
                    if(absX > threshold){
                        increasing = currentValue>0
                        accelerating = false
                    }
                    lastValue = currentValue
                }
            }

            if(!accelerating){
                var pass = false
                if(increasing){
                    if(lastValue>=currentValue){
                        lastAbsValue = absX
                        lastValue = currentValue
                    }else{
                        if(currentValue<-threshold){
                            pass = true
                        }
                        accelerating = true
                    }
                }else{
                    if(lastValue<=currentValue){
                        lastAbsValue = absX
                        lastValue = currentValue
                    }else{
                        if(currentValue>threshold){
                            pass = true
                        }
                        accelerating = true
                    }
                }

                if(pass){
                    return increasing
                }
            }

            return null
        }

        fun reset(){
            accelerating = true
            increasing = false
            lastAbsValue = 0f
            lastValue = 0f
        }
    }
}