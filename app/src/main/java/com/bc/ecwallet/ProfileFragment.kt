package com.bc.ecwallet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment:Fragment() {
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
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_profile,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val name = sharedPref?.getString("name","name")
        val email = sharedPref?.getString("email","email")
        val username = sharedPref?.getString("userName","username")
        val nickname = sharedPref?.getString("nickname","nickname")
        val phone = sharedPref?.getString("phone_number","phone")
        val birthdate = sharedPref?.getString("birthdate","birthdate")

        view.findViewById<TextView>(R.id.txt_name).text = name
        view.findViewById<TextView>(R.id.txt_email).text = email
        view.findViewById<TextView>(R.id.txt_username).text = username
        view.findViewById<TextView>(R.id.txt_nickname).text = nickname
        view.findViewById<TextView>(R.id.txt_phone).text = phone
        view.findViewById<TextView>(R.id.txt_birthdate).text = birthdate
    }
}