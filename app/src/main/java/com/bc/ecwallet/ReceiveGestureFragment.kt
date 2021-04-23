package com.bc.ecwallet

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amplifyframework.core.Amplify
import com.bc.ecwallet.utils.AWSApiCaller
import com.bc.ecwallet.utils.AccelerometerListener
import com.bc.ecwallet.utils.GesturePatternView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReceiveGestureFragment : Fragment(){
    val args:ReceiveGestureFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_receive_gesture,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.txt_title).text = "Receiving $${args.amount.toString()} from ${args.payer}"

        val sm = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val listener = AccelerometerListener()

        val btnStart = view.findViewById<MaterialButton>(R.id.btn_start)
        val viewGesture = view.findViewById<GesturePatternView>(R.id.view_gesture)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btn_retry)
        val btnSend = view.findViewById<MaterialButton>(R.id.btn_send)

        val listGesture = arrayListOf<Int>()
        var isStartRecording = false

        btnStart.addOnCheckedChangeListener { _, _ ->
            btnStart.visibility = View.GONE
            viewGesture.visibility = View.VISIBLE
            btnRetry.visibility = View.VISIBLE
            btnSend.visibility = View.VISIBLE

            sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
            isStartRecording = true
            listener.callback = {
                viewGesture.addGesture(it)
                listGesture.add(it)
                if(listGesture.size >= viewGesture.itemNumber){
                    sm.unregisterListener(listener)
                    isStartRecording = false
                }
            }
        }

        btnRetry.addOnCheckedChangeListener{_,_->
            listGesture.removeAll(listGesture)
            viewGesture.clear()

            if(!isStartRecording){
                sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
                isStartRecording = true
            }
        }

        btnSend.addOnCheckedChangeListener{_,_->
            btnSend.text = "Sending..."

            if(listGesture.size>=4){
                var gesture = ""
                listGesture.iterator().forEach {
                    gesture += it.toString()
                }
                GlobalScope.launch {
                    try{
                        val result = AWSApiCaller.finishPayment(args.transactionId,args.payer,Amplify.Auth.currentUser.username,args.amount,gesture)
                        if (result){
                            Snackbar.make(view,"Payment success", Snackbar.LENGTH_SHORT).show()
                        }else{
                            Snackbar.make(view,"Error occurred! Please try again", Snackbar.LENGTH_SHORT).show()
                        }
                    }catch (e: AWSApiCaller.AWSResourceNotFoundException){
                        Snackbar.make(view,e.message.toString().filterNot{ it == '\"' }, Snackbar.LENGTH_SHORT).show()
                    }

                    activity?.runOnUiThread {
                        findNavController().navigate(R.id.action_receiveGestureFragment_to_mainFragment)
                    }
                }
            }else{
                Snackbar.make(view,"Please finish the gesture!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}