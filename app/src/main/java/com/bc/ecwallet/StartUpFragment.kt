package com.bc.ecwallet

import android.content.res.Resources.getSystem
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bc.ecwallet.utils.SignUpPagerFragmentAdapter
import kotlinx.android.synthetic.main.fragment_startup.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StartUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()
    val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()
    lateinit var navController: NavController

    lateinit var signUpPagerFragmentAdapter: SignUpPagerFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navController = findNavController()

        val view = inflater.inflate(R.layout.fragment_startup, container, false)
        signUpPagerFragmentAdapter=
            SignUpPagerFragmentAdapter(this)

        lateinit var listener:ViewTreeObserver.OnGlobalLayoutListener
        listener=ViewTreeObserver.OnGlobalLayoutListener {
            val img_start_logo=view.findViewById<ImageView>(R.id.img_start_logo)
            val txt_start_slogan=view.findViewById<TextView>(R.id.txt_start_slogan)
            val btn_start_login=view.findViewById<Button>(R.id.btn_start_login)
            val btn_start_signup=view.findViewById<Button>(R.id.btn_start_signup)

            var img_pos_Y = img_start_logo.y
            var img_height = img_start_logo.height
            txt_start_slogan.y = img_pos_Y + img_height + 20.px
            txt_start_slogan.x = (view.width / 2 - txt_start_slogan.width / 2).toFloat()
            btn_start_login.y = (view.height - btn_start_login.height - 20.px).toFloat()
            btn_start_login.x = ((view.width-btn_start_login.width)/2).toFloat()
            btn_start_signup.y = (btn_start_login.y - btn_start_login.height - 10.px)
            btn_start_signup.x = ((view.width-btn_start_signup.width)/2).toFloat()


            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager_start.adapter = signUpPagerFragmentAdapter

        btn_start_signup.addOnCheckedChangeListener{_,isChecked->
            if(isChecked) {
                btn_start_signup.isChecked=false
                btn_start_signup.clearOnCheckedChangeListeners()
                navController.navigate(R.id.action_StartUpFragment_to_signUpFragment)
            }
        }

        btn_start_login.addOnCheckedChangeListener{_,isChecked ->
            if(isChecked) {
                btn_start_login.isChecked=false
                btn_start_login.clearOnCheckedChangeListeners()
                navController.navigate(R.id.action_StartUpFragment_to_loginFragment)
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StartUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}