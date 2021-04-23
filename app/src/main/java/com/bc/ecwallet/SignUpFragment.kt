package com.bc.ecwallet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import at.favre.lib.crypto.bcrypt.BCrypt
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.bc.ecwallet.utils.SignUpPagerFragmentAdapter
import com.bc.ecwallet.utils.SignUpViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_signup.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            if(pager_signup.currentItem==0){
                findNavController().navigateUp()
            }else{
                pager_signup.currentItem=pager_signup.currentItem-1
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        lateinit var listener:ViewTreeObserver.OnGlobalLayoutListener
        listener=ViewTreeObserver.OnGlobalLayoutListener {
            pager_signup.layoutParams.height =
            (view.height - img_signup_logo.height - img_signup_logo.y).toInt()
            pager_signup.requestLayout()

            pager_signup.y = img_signup_logo.y + img_signup_logo.height
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adopter = SignUpPagerFragmentAdapter(this)
        pager_signup.adapter = adopter
        pager_signup.currentItem = 0
        pager_signup.isUserInputEnabled = false

        viewModel.liveData.observe(viewLifecycleOwner, Observer { it ->
            val data = viewModel.liveData.value
            if (it.size == 8) {
                val pw= BCrypt.withDefaults().hashToString(6,data?.get("paymentPassword").toString().toCharArray())

                Amplify.Auth.signUp(data?.get("username").toString(),
                    data?.get("password").toString(),
                    AuthSignUpOptions.builder().userAttributes(
                        listOf(
                            AuthUserAttribute(
                                AuthUserAttributeKey.email(),
                                data?.get("email").toString()
                            ),
                            AuthUserAttribute(
                                AuthUserAttributeKey.name(),
                                data?.get("name").toString()
                            ),
                            AuthUserAttribute(
                                AuthUserAttributeKey.nickname(),
                                data?.get("nickname").toString()
                            ),
                            AuthUserAttribute(
                                AuthUserAttributeKey.birthdate(),
                                data?.get("birthdate").toString()
                            ),
                            AuthUserAttribute(
                                AuthUserAttributeKey.phoneNumber(),
                                data?.get("phoneNumber").toString()
                            ),
                            AuthUserAttribute(
                                AuthUserAttributeKey.custom("custom:payment_password"),
                                pw
                            )
                        )
                    )
                        .build(),
                    { result -> Log.d("AuthQuickStartSignUp", "Result: $result") },
                    { error -> Log.e("AuthQuickStartSignUp", "Sign up failed", error) }
                )

                pager_signup.currentItem = pager_signup.currentItem + 1
            } else if (it.size == 9) {
                Amplify.Auth.confirmSignUp(
                    data?.get("username").toString(),
                    data?.get("verificationCode").toString(),
                    { result ->
                        Log.i("AuthQuickstart",if (result.isSignUpComplete) "Confirm signUp succeeded" else "Confirm sign up not complete")
                        Snackbar.make(view,"Sign up success!",Snackbar.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                        }, 1000)
                    },
                    { error ->
                        Log.e("AuthQuickstart", error.toString())
                        if(error.cause is com.amazonaws.services.cognitoidentityprovider.model.AliasExistsException){
                            Snackbar.make(view,"Account already exist!",Snackbar.LENGTH_SHORT).show()
                        }else{
                            Snackbar.make(view,"Sign up failed Please try again!",Snackbar.LENGTH_SHORT).show()
                        }
                    }
                )
                Amplify.Auth.fetchAuthSession(
                    { result -> Log.i("AmplifyQuickstartSession", result.toString()) },
                    { error -> Log.e("AmplifyQuickstartSession", error.toString()) }
                )
                Log.d("","")
            } else {
                pager_signup.currentItem = pager_signup.currentItem + 1
            }
        }
        )
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}