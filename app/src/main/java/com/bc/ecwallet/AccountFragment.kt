package com.bc.ecwallet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.core.Amplify
import com.bc.ecwallet.utils.DefaultViewRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_account,container,false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_account.layoutManager=LinearLayoutManager(context)
        val adapter = DefaultViewRecyclerAdapter(context)
        adapter.addItem("Profile",R.drawable.ic_account_circle)
        adapter.addItem("Log out",R.drawable.ic_logout)
        recycler_account.adapter=adapter
        val decoration=DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
        recycler_account.addItemDecoration(decoration)
        adapter.addOnItemClickListener{
            when(it){
                "Log out"->{
                    Amplify.Auth.signOut(
                        {
                            Log.d("AuthQuickstartSignOut","Success")
                            Amplify.Auth.fetchUserAttributes(
                                {
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
                            findNavController().navigate(R.id.action_mainFragment_to_startUpFragment)
                        },
                        {
                            Log.d("AuthQuickstartSignOut","Failed")
                            Snackbar.make(view,"Log out failed!",Snackbar.LENGTH_SHORT).show()
                        }
                    )
                }
                "Profile"->{
                    findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
                }
            }
        }

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = ""
        txt_user_name.text = sharedPref.getString("name", defaultValue)
    }
}