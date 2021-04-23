package com.bc.ecwallet.utils

import android.content.Context
import android.util.Log
import android.view.View
import com.amplifyframework.core.Amplify
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList


object AWSApiCaller {
    var url = "https://28ya0vu0ve.execute-api.ap-northeast-1.amazonaws.com/test/"
    private var queue: RequestQueue? = null

    fun init(context: Context) {
        queue = Volley.newRequestQueue(context)
    }



    public fun checkSavings(userName: String,callback:(Double)->Unit) {
        val url = url + "check-saving"


        val jsonObject = JSONObject()
        jsonObject.put("userName", userName)


        sendRequest(url,jsonObject){
            when (it.get("statusCode")) {
                200 -> {
                    callback(it.getDouble("saving"))
                }
                404 -> {
                    throw AWSResourceNotFoundException("Username \"$userName\" is not found")
                }
            }
        }
    }

    public suspend fun checkSavings(userName:String):Double{
        val url = url + "check-saving"


        val jsonObject = JSONObject()
        jsonObject.put("userName", userName)

        val result = sendRequest(url,jsonObject)
        return if (result != null) {
            when (result.get("statusCode")) {
                200 -> {
                    result.getDouble("saving")
                }
                404 -> {
                    throw AWSResourceNotFoundException("Username \"$userName\" is not found")
                }
                else->{
                    -1.0
                }
            }
        }else{
            -1.0
        }
    }

    public fun getHistory(userName: String,numberRecords: Int,callback:((ArrayList<PaymentHistory>)->Unit)){
        val url = url + "check-records"

        val jsonObject = JSONObject()
        jsonObject.put("userName", userName)
        jsonObject.put("numberRecords",numberRecords)

        sendRequest(url,jsonObject){
            when (it.get("statusCode")) {
                200 -> {
                    val paymentHistories = arrayListOf<PaymentHistory>()
                    val histories = it.getJSONArray("paymentRecords")
                    for(i in 0 until histories.length()){
                        val record = histories[i] as JSONObject
                        if(record.getString("payer").equals(userName)){
                            paymentHistories.add(PaymentHistory(record.getString("transactionId"),record.getString("payee"),record.getString("time"),record.getDouble("amount"),record.getBoolean("verified"),PaymentHistory.PAYING))
                        }else{
                            paymentHistories.add(PaymentHistory(record.getString("transactionId"),record.getString("payer"),record.getString("time"),record.getDouble("amount"),record.getBoolean("verified"),PaymentHistory.RECEIVING))
                        }
                    }

                    callback(paymentHistories)
                }
                404 -> {
                    throw AWSResourceNotFoundException("No record found")
                }
            }
        }
    }

    public suspend fun getHistory(userName: String, numberRecords: Int):ArrayList<PaymentHistory>?{
        val url = url + "check-records"

        val jsonObject = JSONObject()
        jsonObject.put("userName", userName)
        jsonObject.put("numberRecords",numberRecords)

        val result = sendRequest(url, jsonObject)

        return if (result != null) {
            when (result.get("statusCode")) {
                200 -> {
                    val paymentHistories = arrayListOf<PaymentHistory>()
                    val histories = result.getJSONArray("paymentRecords")
                    for(i in 0 until histories.length()){
                        val record = histories[i] as JSONObject
                        if(record.getString("payer").equals(userName)){
                            paymentHistories.add(PaymentHistory(record.getString("transactionId"),record.getString("payee"),record.getString("time"),record.getDouble("amount"),record.getBoolean("verified"),PaymentHistory.PAYING))
                        }else{
                            paymentHistories.add(PaymentHistory(record.getString("transactionId"),record.getString("payer"),record.getString("time"),record.getDouble("amount"),record.getBoolean("verified"),PaymentHistory.RECEIVING))
                        }
                    }

                    paymentHistories
                }
                404 -> {
                    throw AWSResourceNotFoundException("Username \"$userName\" is not found")
                }
                else->{
                    null
                }
            }
        }else{
            null
        }
    }

    public suspend fun getNonce():Long{
        val url = this.url + "get-nonce"
        val jsonObject = JSONObject()

        jsonObject.put("userName",Amplify.Auth.currentUser.username)

        val result = sendRequest(url,jsonObject)

        if(result?.getInt("statusCode") == 200){
            Log.d("nonce","nonce get!")
            val md = MessageDigest.getInstance("SHA-256")
            md.update(Amplify.Auth.currentUser.userId.toByteArray())
            val digest = md.digest()

            val key = SecretKeySpec(digest,"")
            val data = result?.getLong("nonce").toString()

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)

            val final = mac.doFinal(data.toByteArray())
            val output = String(Base64.getEncoder().encode(final))

            val signature = result!!.getString("hmac")?:""

            if(signature == output){
                return result!!.getLong("nonce")
            }else{
                return -1
            }
        }else{
            val warning = result?.getString("warning")?:"error"
            Log.d("getNonceWarning",warning)
            return -1
        }
    }

    public suspend fun checkUserName(userName: String):Boolean{
        val url = this.url + "check-username"
        val jsonObject = JSONObject()

        jsonObject.put("userName",userName)

        val result = sendRequest(url,jsonObject)

        return result!!.getBoolean("available")
    }

    public suspend fun checkEmail(email:String):Boolean{
        val url = this.url + "check-email"
        val jsonObject = JSONObject()

        jsonObject.put("email",email)

        val result = sendRequest(url,jsonObject)

        return result!!.getBoolean("available")
    }

    public suspend fun checkPhone(phone:String):Boolean{
        val url = this.url + "check-phone"
        val jsonObject = JSONObject()

        jsonObject.put("phone",phone)

        val result = sendRequest(url,jsonObject)

        return result!!.getBoolean("available")
    }

    public suspend fun newPayment(payee:String,amount:Float,gesture:String,paymentPassword:String):Boolean{
        val url = this.url + "new-payment"
        val jsonObject = JSONObject()

        val nonce = getNonce()

        val message = "$nonce|${Amplify.Auth.currentUser.username}|$payee|$amount|$gesture|$paymentPassword|ECWallet"
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Amplify.Auth.currentUser.userId.toByteArray())
        val digest = md.digest()
        val key = SecretKeySpec(digest,"")

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(key)

        val hmac = mac.doFinal(message.toByteArray())
        val encoded = String(Base64.getEncoder().encode(hmac))

        jsonObject.put("payer",Amplify.Auth.currentUser.username)
        jsonObject.put("payee",payee)
        jsonObject.put("amount",amount.toString())
        jsonObject.put("paymentPassword",paymentPassword)
        jsonObject.put("gesture",gesture)
        jsonObject.put("hmac",encoded)

        val result = sendRequest(url,jsonObject,30000)

        if(result != null){
            return when(result.getInt("statusCode")){
                200->{
                    return true
                }
                406->{
                    throw AWSResourceNotFoundException(result.getString("warning"))
                }
                else-> false
            }
        }else{
            return false
        }
    }

    public suspend fun finishPayment(transactionId:String,payer:String,payee:String,amount: Float,gesture: String):Boolean{
        val url = this.url+ "finish-payment"
        val jsonObject = JSONObject()

        jsonObject.put("transactionId",transactionId)
        jsonObject.put("payer",payer)
        jsonObject.put("payee",payee)
        jsonObject.put("amount",amount)
        jsonObject.put("gesture",gesture)

        val result = sendRequest(url,jsonObject)

        if(result != null){
            return when(result.getInt("statusCode")){
                200->{
                    return true
                }
                406->{
                    throw AWSResourceNotFoundException(result.getString("warning"))
                }
                else-> false
            }
        }else{
            return false
        }
    }

    public suspend fun checkTransactions(user:String):ArrayList<Transaction>{
        val url = this.url+ "check-transactions"
        val jsonObject = JSONObject()

        jsonObject.put("user",user)

        val result = sendRequest(url,jsonObject)

        if(result != null){
            return when(result.getInt("statusCode")){
                200->{
                    val transactions = arrayListOf<Transaction>()
                    val records = result.getJSONArray("records")
                    Log.d("checkTransactions",result.toString())

                    for(i in 0 until records.length()){
                        val record = records[i] as JSONObject
                        transactions.add(Transaction(record.getString("transactionId"),record.getString("payer"),record.getDouble("amount").toFloat()))
                    }

                    transactions
                }
                else->{
                    throw AWSResourceNotFoundException(result.getString("warning"))
                }
            }
        }else{
            throw AWSException("Unexpected error.")
        }
    }

    public fun submitPayment(view: View, nearbyPayment: NearbyPayment){
        val url = this.url + "payment"
        val jsonObject = JSONObject()

        jsonObject.put("transactionId",nearbyPayment.getTransactionId())
        jsonObject.put("payer",nearbyPayment.getPayer())
        jsonObject.put("payee",nearbyPayment.getPayee())
        jsonObject.put("amount",nearbyPayment.getAmount())

        val verificationDataJson = JSONArray()
        nearbyPayment.getVerificationData()?.iterator()?.forEach {
            verificationDataJson.put(it)
        }
        jsonObject.put("verificationData",verificationDataJson)

        if(nearbyPayment is NearbyPayer){
            jsonObject.put("paymentPassword",nearbyPayment.getPaymentPassword())
            jsonObject.put("paymentType",0)

        }else{
            jsonObject.put("paymentType",1)
        }

        sendRequest(url,jsonObject,30000){
            when (it.get("statusCode")) {
                200 -> {
                    val response = it.getString("response")
                    if(response.contains("Error")){
                        Snackbar.make(view,response,Snackbar.LENGTH_SHORT).show()
                    }else{
                        Snackbar.make(view,"Success",Snackbar.LENGTH_SHORT).show()
                    }
                }
                404 -> {
                    throw AWSResourceNotFoundException("No record found")
                }
            }
        }
        Log.d("jsonLog",jsonObject.toString())
    }

    public suspend fun getServerKey():String{
        val url = this.url + "cert"

        val result = sendRequest(url,JSONObject())
        if(result != null){
            when(result.getInt("statusCode")){
                200-> return result.getString("key")
                else-> return ""
            }
        }else{
            return ""
        }
    }

    private fun sendRequest(url: String, jsonObject: JSONObject,timeout:Int = 3000,callback:((JSONObject)->Unit)){
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObject,
            Response.Listener {
                Log.d("api_response", it.toString())
                callback(it)
            },
            Response.ErrorListener {
                throw it
            }
        )
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            timeout,
            3,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue?.add(jsonObjectRequest)
    }

    private suspend fun sendRequest(url: String, jsonObject: JSONObject,timeout:Int = 3000,timeDelay:Long = 1000):JSONObject?{
        var result:JSONObject? = null

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObject,
            Response.Listener {
                Log.d("api_response", it.toString())
                result = it
            },
            Response.ErrorListener {
                throw it
            }
        )
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            timeout,
            3,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue?.add(jsonObjectRequest)

        runBlocking {
            while(true){
                if(result == null){
                    delay(timeDelay)
                }else{
                    break
                }
            }
        }

        return result
    }


    class AWSException(message: String) : Exception(message)
    class AWSResourceNotFoundException(message: String) : Exception(message)
    class AWSPaymentFailedException(message:String) : Exception(message)
}