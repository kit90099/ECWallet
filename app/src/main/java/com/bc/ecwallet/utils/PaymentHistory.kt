package com.bc.ecwallet.utils

import java.text.SimpleDateFormat
import java.util.*

class PaymentHistory {
    var transactionId:String
    var target:String
    var time:Calendar
    var amount:Double
    var status:Boolean
    var type:Int

    constructor(transactionId:String,target:String,time:String,amount:Double,status:Boolean,type:Int){
        this.transactionId = transactionId
        this.target = target
        this.amount = amount
        this.status = status
        this.type = type

        val cal:Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("MM/dd/yyyy, HH:mm:ss",Locale.ENGLISH)
        cal.time = sdf.parse(time)
        this.time = cal
    }

    /*constructor(target:String,time:String,amount:Double,status:Int,type:Int){
        this.target = target
        this.amount = amount
        this.status = status
        this.type = type

        val formatter = DateTimeFormatter.ofPattern("")
    }*/

    override fun equals(other: Any?): Boolean {
        val comparing = other as PaymentHistory
        return comparing.transactionId == transactionId
    }


    companion object{
        val PAYING = 0
        val RECEIVING = 1
        val SUCCEED = 1
        val FAILED = 0
    }
}