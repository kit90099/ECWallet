package com.bc.ecwallet.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.amplifyframework.core.Amplify
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import kotlin.collections.ArrayList

public class NearbyPayee:NearbyPayment{
    @Transient var connectionsClient: ConnectionsClient? = null
    var currentEndPointId: String? = null
    @Transient var onAmountReceivedListener: (()->Unit)? = null
    @Transient var onConnectedCallback:(()->Unit)? = null
    @Transient var onPayerRequestCallback:((String,String)->Unit)? = null

    var mPayer:String? = null
    var mTransactionId: String? = null
    var mAmount: Float? = null
    var mVerificationData = arrayListOf<Long>()

    @Transient var mConnectionLifecycleCallback:ConnectionLifecycleCallback

    constructor(context: Context) : super() {
        connectionsClient = Nearby.getConnectionsClient(context)

        mConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {


                /*connectionsClient!!
                    .acceptConnection(
                        endpointId,
                        ReceiveBytesPayloadListener(
                            onMessageReceived
                        )
                    )*/
                mPayer = connectionInfo.endpointName
                currentEndPointId = endpointId
                onPayerRequestCallback?.invoke(connectionInfo.endpointName,"")
                /*onConnectedCallback?.let{
                    it()
                }*/
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.
                        onConnectedCallback?.invoke()
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // The connection was rejected by one or both sides.
                        val a =1
                    }
                    ConnectionsStatusCodes.ERROR -> {
                        // The connection broke before it was able to be accepted.
                        val a =1
                    }
                    else -> {
                        // Unknown status code
                    }
                }
            }

            override fun onDisconnected(p0: String) {
                Snackbar.make((context as Activity).findViewById<View>(android.R.id.content),"Connection ended!",Snackbar.LENGTH_SHORT).show()
                /*val navController = (context as Activity).findNavController(R.id.nav_fragment)
                while(true){
                    if(navController.currentDestination?.id == R.id.mainFragment){
                        break
                    }else{
                        navController.navigateUp()
                    }
                }*/
            }
        }
    }

    fun startAdvertising(context: Context) {

        Amplify.Auth.fetchUserAttributes(
            {
                Log.d("","")
            },
            {
                Log.d("","")
            }
        )

        val advertisingOptions =
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        if (connectionsClient != null && mConnectionLifecycleCallback != null) {
            connectionsClient!!.startAdvertising(
                Amplify.Auth.currentUser.username, context.packageName,
                mConnectionLifecycleCallback!!, advertisingOptions
            )
                .addOnSuccessListener { Log.d("advertising_result", "success")
                }
                .addOnFailureListener { exception ->
                    Log.d("advertising_result", "failed")
                    Log.e("error", exception.toString())
                }
        }
    }

    fun acceptConnection(accept:Boolean){
        if(accept){
            if(currentEndPointId != null){
                connectionsClient?.acceptConnection(
                    currentEndPointId!!,
                    ReceiveBytesPayloadListener(onMessageReceived)
                )
            }

        }else{
            if(currentEndPointId != null){
                connectionsClient?.rejectConnection(currentEndPointId!!)
            }
            currentEndPointId = null
        }
    }


    override fun endConnection() {
        connectionsClient?.stopAllEndpoints()
        connectionsClient = null
        currentEndPointId = null
    }

    fun addOnAmountReceivedListener(onAmountReceivedListener:(()->Unit)){
        this.onAmountReceivedListener = onAmountReceivedListener
    }

    fun addOnConnectedCallback(onConnectedCallback:(()->Unit)){
        this.onConnectedCallback = onConnectedCallback
    }

    fun addOnPayerRequestCallback(callback:((String,String)->Unit)){
        onPayerRequestCallback = callback
    }

    @Transient val onMessageReceived: OnReceivedCallback = object :
        OnReceivedCallback {
        override fun onMessageReceived(message: String) {
            val messages = message.split(":")
            when(messages[0]){
                "transactionId"->{
                    mTransactionId = messages[1]
                }
                "amount"->{
                    mAmount = messages[1].toFloat()
                    onAmountReceivedListener?.let {
                        it()
                    }
                }
            }
        }
    }

    override fun getTransactionId(): String? {
        return mTransactionId
    }

    override fun getPayer(): String? {
        return mPayer
    }

    override fun getPayee(): String? {
        return Amplify.Auth.currentUser.username
    }

    override fun getAmount(): Float? {
        return mAmount
    }

    override fun getVerificationData(): ArrayList<Long>? {
        return mVerificationData
    }

    override fun getPaymentPassword(): String? {
        return null
    }

    override fun setVerificationData(verificationData: ArrayList<Long>) {
        mVerificationData = verificationData
    }
}