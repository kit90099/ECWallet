package com.bc.ecwallet.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {
    val liveData=MutableLiveData<MutableMap<String,String>>()

    val addData={ key:String ,data:String->
        var userData=liveData.value

        if(userData==null){
            userData = mutableMapOf(key to data)
        }else {
            userData?.put(key, data)
        }

        liveData.value=userData
    }

}