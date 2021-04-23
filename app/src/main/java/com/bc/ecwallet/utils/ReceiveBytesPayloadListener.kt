package com.bc.ecwallet.utils

import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.nio.charset.StandardCharsets

class ReceiveBytesPayloadListener(val callback: OnReceivedCallback): PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        val receivedBytes = payload.asBytes()
        if(receivedBytes!=null){
            callback.onMessageReceived(String(receivedBytes, StandardCharsets.UTF_8))
        }
    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        Log.d("do mud 9","")
    }
}