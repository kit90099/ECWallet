package com.bc.ecwallet

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment(): Fragment() {
    val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
                findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_login, container, false)

        lateinit var listener:ViewTreeObserver.OnGlobalLayoutListener
        listener=ViewTreeObserver.OnGlobalLayoutListener {
            txt_login.y = img_login_logo.y + img_login_logo.height + 20.px
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val inputCheck={builder: Editable?->
            val userName:String=tb_login_user.editText?.text.toString()
            val pw:String=tb_login_pw.editText?.text.toString()

            if(pw.length<16||userName.length==0){
                btn_login.isCheckable=false
                btn_login.isClickable=false
                btn_login.setTextColor(Color.parseColor("#8DA5A5"))
                btn_login.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.colorComponentBackground))
            }else{
                btn_login.isCheckable=true
                btn_login.isClickable=true
                btn_login.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorBackgroundEnd))
                btn_login.setBackgroundColor(Color.WHITE)
            }
        }

        tb_login_user.editText?.addTextChangedListener { it->
            inputCheck(it)
        }

        tb_login_pw.editText?.addTextChangedListener { it->
            inputCheck(it)
        }

        btn_login.addOnCheckedChangeListener{_,_->
            btn_login.text = "Loading..."

            Amplify.Auth.signIn(
                tb_login_user.editText?.text.toString(),
                tb_login_pw.editText?.text.toString(),
                { result ->
                    Log.d("AuthQuickstartSignIn", if (result.isSignInComplete) "Sign in succeeded" else "Sign in not complete")
                    Amplify.Auth.fetchUserAttributes(

                        {
                            it
                            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                            with(sharedPref?.edit()){
                                this?.putString("userName",Amplify.Auth.currentUser.username)
                                it.forEach {
                                    this?.putString(it.key.keyString,it.value.toString())
                                    this?.apply()
                                }
                            }
                        },
                        {

                        }
                    )

                    activity?.runOnUiThread {
                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                    }
                },
                { error ->
                    Log.d("AuthQuickstartSignIn", error.toString())
                    when((error.cause!!)::class.java.name){
                        "com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException"->{
                            Snackbar.make(view,"Incorrect username or password!",Snackbar.LENGTH_SHORT).show()
                            btn_login.text = "CONTINUE"
                        }
                    }
                    btn_login.text = "CONTINUE"
                }

            )
        }
    }

    private fun initializeView(){
        var listener:MaterialButton.OnCheckedChangeListener
        listener=MaterialButton.OnCheckedChangeListener{
                _, _ ->
            Log.d("","")

            Amplify.Auth.signIn(
                "testuser",
                "Qwerty1234",
                { result ->
                    Log.d("AuthQuickstartSignIn", if (result.isSignInComplete) "Sign in succeeded" else "Sign in not complete")


                    findNavController().navigate(R.id.action_startUpFragment_to_mainFragment)
                },
                { error -> Log.e("AuthQuickstartSignIn", error.toString()) }
            )
        }

        btn_login.addOnCheckedChangeListener(listener)


        /*btn_login.setOnClickListener { _ ->
            Amplify.Auth.signIn(
                "testuser",
                "Qwerty1234",
                { result ->
                    Log.d("AuthQuickstartSignIn", if (result.isSignInComplete) "Sign in succeeded" else "Sign in not complete")

                },
                { error -> Log.e("AuthQuickstartSignIn", error.toString()) }
            )


            Amplify.Auth.fetchAuthSession(
                { result -> Log.i("AmplifyQuickstartFetch", result.toString())
                },
                { error -> Log.e("AmplifyQuickstartFetch", error.toString()) }
            )
        }*/

        /*btn_login.addOnCheckedChangeListener(MaterialButton.OnCheckedChangeListener(){

            override fun onClick(v: View?) {

                val user=Amplify.Auth.currentUser

                Amplify.Auth.signIn(
                    "testuser",
                    "Qwerty1234",
                    { result ->
                        Log.d("AuthQuickstartSignIn", if (result.isSignInComplete) "Sign in succeeded" else "Sign in not complete")

                    },
                    { error -> Log.e("AuthQuickstartSignIn", error.toString()) }
                )


                Amplify.Auth.fetchAuthSession(
                    { result -> Log.i("AmplifyQuickstartFetch", result.toString())
                    },
                    { error -> Log.e("AmplifyQuickstartFetch", error.toString()) }
                )

            }
        })*/
    }
}