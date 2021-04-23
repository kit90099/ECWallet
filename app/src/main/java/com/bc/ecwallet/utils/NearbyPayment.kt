package com.bc.ecwallet.utils;

import java.io.Serializable

public abstract class NearbyPayment: Serializable {
    public abstract fun getTransactionId():String?
    public abstract fun getPayer():String?
    public abstract fun getPayee():String?
    public abstract fun getAmount():Float?
    public abstract fun getVerificationData():ArrayList<Long>?
    public abstract fun getPaymentPassword():String?
    public abstract fun setVerificationData(verificationData:ArrayList<Long>)
    public abstract fun endConnection()
}
