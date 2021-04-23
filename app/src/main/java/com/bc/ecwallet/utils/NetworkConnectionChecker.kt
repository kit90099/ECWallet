package com.bc.ecwallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkConnectionChecker {
    companion object{
        var context:Context? = null

        fun init(context:Context){
            this.context = context
        }

        fun checkNetworkConnection():Boolean{
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            return isConnected
        }
    }
}