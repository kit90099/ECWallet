package com.bc.ecwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class PaymentFragment: Fragment() {
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
        return inflater.inflate(R.layout.fragment_payment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btn_continue).addOnCheckedChangeListener{_,_->
            val payee = view.findViewById<TextInputLayout>(R.id.tb_username).editText?.text.toString()
            val amount = view.findViewById<TextInputLayout>(R.id.tb_amount).editText?.text.toString()
            val paymentPassword = view.findViewById<TextInputLayout>(R.id.tb_password).editText?.text.toString()


            if(amount.toFloat()<=0){
                Snackbar.make(view,"Amount must greater than 0!",Snackbar.LENGTH_SHORT).show()
            }

            if(
                payee.length>0 &&
                amount.length>0
            ){
                val action = PaymentFragmentDirections.actionPaymentFragmentToPayGestureFragment(payee,amount.toFloat(),paymentPassword)
                findNavController().navigate(action)
            }else{
                Snackbar.make(view,"Please input all fields!",Snackbar.LENGTH_SHORT).show()
            }
        }

    }
}