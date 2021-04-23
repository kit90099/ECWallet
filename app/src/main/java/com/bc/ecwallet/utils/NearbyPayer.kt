package com.bc.ecwallet.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.amplifyframework.core.Amplify
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class NearbyPayer:NearbyPayment{
    @Transient var connectionsClient:ConnectionsClient? = null
    var currentEndPointId:String? = null
    @Transient var onNearbyUsersChangeListener: ((String) -> Unit)? = null
    @Transient var onConnectedListener:((Int)->Unit)? = null
    var deviceList = arrayListOf<NearbyUsers>()

    var mTransactionId:String = ""
    var mPayee = ""
    var mAmount = 0f
    var mPaymentPassword:String = ""
    var mVerificationData = arrayListOf<Long>()

    @Transient var mConnectionLifecycleCallback: ConnectionLifecycleCallback


    constructor(context: Context) {
        connectionsClient = Nearby.getConnectionsClient(context)
        mConnectionLifecycleCallback= object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String,result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.
                        onConnectedListener?.invoke(SUCCESS)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // The connection was rejected by one or both sides.
                        onConnectedListener?.invoke(REJECTED)
                    }
                    ConnectionsStatusCodes.ERROR -> {
                        // The connection broke before it was able to be accepted.
                        onConnectedListener?.invoke(FAILED)
                    }
                    else -> {
                        // Unknown status code
                        onConnectedListener?.invoke(FAILED)
                    }
                }
            }

            override fun onDisconnected(p0: String) {
                Snackbar.make((context as Activity).findViewById<View>(android.R.id.content),"Connection ended!",
                    Snackbar.LENGTH_SHORT).show()
                /*val navController = (context as Activity).findNavController(R.id.nav_fragment)
                while(true){
                    if(navController.currentDestination?.id == R.id.mainFragment){
                        break
                    }else{
                        navController.navigateUp()
                    }
                }*/
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                connectionsClient!!
                    .acceptConnection(
                        endpointId,
                        ReceiveBytesPayloadListener(
                            onMessageReceived
                        )
                    )
                currentEndPointId = endpointId
                mPayee = connectionInfo.endpointName

                val calendar = Calendar.getInstance(Locale.getDefault())
                /*
                * 1st: payer
                * 2nd: payee
                * 3rd: yy
                * 4th: mm
                * 5th: dd
                * 6th: hh
                * 7th: mm
                * 8th: ss
                * */
                mTransactionId = String.format("%s,%s,%d%02d%02d%02d%02d%02d",
                    Amplify.Auth.currentUser.username,
                    connectionInfo.endpointName,
                    calendar.weekYear%100,
                    calendar[Calendar.MONTH]+1,
                    calendar[Calendar.DAY_OF_MONTH],
                    calendar[Calendar.HOUR_OF_DAY],
                    calendar[Calendar.MINUTE],
                    calendar[Calendar.SECOND]
                )


            }

        }
    }

    @Transient var mEndpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            deviceList.add(
                NearbyUsers(
                    info.endpointName,
                    endpointId
                )
            )
            onNearbyUsersChangeListener?.let { it(info.endpointName) }
        }

        override fun onEndpointLost(p0: String) {
        }

    }





    override fun endConnection(){
        connectionsClient?.stopAllEndpoints()
        connectionsClient = null
        currentEndPointId = null
    }

    fun startDiscovery(context: Context) {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        if (mEndpointDiscoveryCallback != null && connectionsClient !=null) {
            connectionsClient!!.startDiscovery(
                context.packageName,
                mEndpointDiscoveryCallback!!, discoveryOptions
            )
                .addOnSuccessListener { Log.d("discovery_result", "success") }
                .addOnFailureListener { exception ->
                    Log.d("discovery_result", "failed")
                    Log.e("error", exception.toString())
                }
        }
    }

    fun requestConnection(endpointId: String) {

        if (mConnectionLifecycleCallback != null && connectionsClient != null) {
            connectionsClient!!
                .requestConnection(
                    Amplify.Auth.currentUser.username,
                    endpointId,
                    mConnectionLifecycleCallback!!
                )
                .addOnSuccessListener {  }
                .addOnFailureListener { exception ->
                    Log.e("error", exception.toString())
                }

        }
    }

    fun addOnNearbyUsersChangeListener(callback:(String)->Unit){
        onNearbyUsersChangeListener = callback
    }

    fun sendPayload(data:String){
        val payload = Payload.fromBytes(data.toByteArray())

        if(connectionsClient != null && currentEndPointId != null){
            connectionsClient!!.sendPayload(currentEndPointId!!,payload).addOnSuccessListener {
                Log.d("sendPayload","success")
            }.addOnFailureListener {
                Log.d("sendPayload",it.toString())
            }
        }
    }

    fun addOnConnectedListener(onConnectedListener:(Int)->Unit){
        this.onConnectedListener = onConnectedListener
    }

    @Transient val onMessageReceived: OnReceivedCallback = object:
        OnReceivedCallback {
        override fun onMessageReceived(message: String) {
            val messages = message.split(":")
            when(messages[0]){

            }
        }
    }

    class NearbyUsers(var userName:String,var endPointId:String):Serializable{
        companion object{
            fun findEndPointId(dataList:ArrayList<NearbyUsers>, userName:String): String? {
                dataList.iterator().forEach {
                    if(it.userName == userName){
                        return it.endPointId
                    }
                }

                return null
            }
        }
    }

    override fun getTransactionId(): String? {
        return mTransactionId
    }

    override fun getPayer(): String? {
        return Amplify.Auth.currentUser.username
    }

    override fun getPayee(): String? {
        return mPayee
    }

    override fun getAmount(): Float? {
        return mAmount
    }

    override fun getVerificationData(): ArrayList<Long>? {
        return mVerificationData
    }

    override fun getPaymentPassword(): String? {
        return mPaymentPassword
    }

    override fun setVerificationData(verificationData: ArrayList<Long>) {
        mVerificationData = verificationData
    }

    companion object{
        const val FAILED = 0
        const val REJECTED = 1
        const val SUCCESS = 2
    }
}