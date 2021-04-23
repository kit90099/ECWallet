package com.bc.ecwallet

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bc.ecwallet.utils.AWSApiCaller
import com.bc.ecwallet.utils.SignUpViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_signup_p1.*
import kotlinx.android.synthetic.main.fragment_signup_p1.btn_signup_continue
import kotlinx.android.synthetic.main.fragment_signup_p3.*
import kotlinx.android.synthetic.main.fragment_signup_p7.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

open class SignUpPagerFragment(private val position: Int) : Fragment() {
    private val viewModel: SignUpViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val resId = resources.getIdentifier(
            "fragment_signup_p" + (position + 1).toString(),
            "layout",
            "com.bc.ecwallet"
        )
        val view = inflater.inflate(resId, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        when (position + 1) {
            1, 2, 4, 6 -> {

                tb_signup.editText?.addTextChangedListener { it ->
                    if (it?.length == 0) {
                        btnClickable(false)
                    } else {
                        btnClickable(true)
                    }
                }
            }
            3 -> {
                btnClickable(true)
            }
            5, 9 -> {
                val requiredLen = { -> if (position + 1 == 5) 8 else 6 }
                tb_signup.editText?.addTextChangedListener { it ->
                    if (it?.length == requiredLen()) {
                        btnClickable(true)
                    } else {
                        btnClickable(false)
                    }
                }
            }
            7, 8 -> {
                val inputCheck = { builder: Editable? ->
                    val requiredLen = { -> if (position + 1 == 7) 16 else 6 }
                    val pw: String = tb_signup_pw.editText?.text.toString()
                    val re_pw: String = tb_signup_re_pw.editText?.text.toString()

                    if (builder?.length!! < requiredLen() ||
                        pw != re_pw
                    ) {
                        btnClickable(false)
                    } else {
                        btnClickable(true)
                    }
                }

                tb_signup_pw.editText?.addTextChangedListener { it ->
                    inputCheck(it)
                }

                tb_signup_re_pw.editText?.addTextChangedListener { it ->
                    inputCheck(it)
                }
            }
        }


        btn_signup_continue.addOnCheckedChangeListener { _, _ ->
            when (position + 1) {
                1, 2 -> {
                    viewModel.addData(
                        tb_signup.hint.toString().decapitalize(),
                        tb_signup.editText?.text.toString()
                    )
                }
                3 -> {
                    //val date=String.format("%d/%d/%d",dp_signup_birthday.year,dp_signup_birthday.month+1,dp_signup_birthday.dayOfMonth)
                    val date = LocalDate.of(
                        dp_signup_birthday.year,
                        dp_signup_birthday.month + 1,
                        dp_signup_birthday.dayOfMonth
                    )
                    viewModel.addData(
                        "birthdate",
                        date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    )
                }
                4 ->{
                    GlobalScope.launch {
                        val result = AWSApiCaller.checkEmail(tb_signup.editText?.text.toString())
                        if(!result){
                            Snackbar.make(view,"Not available!",Snackbar.LENGTH_SHORT).show()
                        }else{
                            activity?.runOnUiThread {
                                viewModel.addData(
                                    tb_signup.hint.toString().decapitalize(),
                                    tb_signup.editText?.text.toString()
                                )
                            }
                        }
                    }
                }
                6->{
                    GlobalScope.launch {
                        val result = AWSApiCaller.checkUserName(tb_signup.editText?.text.toString())
                        if(!result){
                            Snackbar.make(view,"Not available!",Snackbar.LENGTH_SHORT).show()
                        }else{
                            activity?.runOnUiThread {
                                viewModel.addData(
                                    tb_signup.hint.toString().decapitalize(),
                                    tb_signup.editText?.text.toString()
                                )
                            }
                        }
                    }
                }
                5 -> {
                    GlobalScope.launch {
                        val result = AWSApiCaller.checkPhone("+852"+tb_signup.editText?.text.toString())
                        if(!result){
                            Snackbar.make(view,"Not available!",Snackbar.LENGTH_SHORT).show()
                        }else{
                            activity?.runOnUiThread { viewModel.addData("phoneNumber", "+852" + tb_signup.editText?.text)}
                        }
                    }
                }
                7, 8 -> {
                    viewModel.addData(
                        tb_signup_pw.hint.toString().decapitalize()
                            .filter { !it.isWhitespace() },
                        tb_signup_pw.editText?.text.toString()
                    )
                }
                9 -> {
                    viewModel.addData("verificationCode", tb_signup.editText?.text.toString())
                }
            }

        }

    }

    val btnClickable = { clickable: Boolean ->
        if (clickable) {
            btn_signup_continue.isCheckable = true
            btn_signup_continue.isClickable = true
            btn_signup_continue.setTextColor(
                getColor(
                    requireActivity(),
                    R.color.colorBackgroundEnd
                )
            )
            btn_signup_continue.setBackgroundColor(Color.WHITE)
        } else {
            btn_signup_continue.isCheckable = false
            btn_signup_continue.isClickable = false
            btn_signup_continue.setTextColor(Color.parseColor("#8DA5A5"))
            btn_signup_continue.setBackgroundColor(
                getColor(
                    requireActivity(),
                    R.color.colorComponentBackground
                )
            )
        }
    }
}