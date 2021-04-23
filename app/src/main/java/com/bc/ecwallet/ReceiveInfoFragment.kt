package com.bc.ecwallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amplifyframework.core.Amplify
import com.bc.ecwallet.utils.AWSApiCaller
import com.bc.ecwallet.utils.PaymentInfoAdapter
import com.bc.ecwallet.utils.Transaction
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class ReceiveInfoFragment: Fragment() {
    var checkingJob : Job? = null

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
        return layoutInflater.inflate(R.layout.fragment_receive_info,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_payee_info)
        recycler.layoutManager = LinearLayoutManager(context)
        val adapter = PaymentInfoAdapter()
        recycler.adapter = adapter

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.layout_swipe)
        swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch {
                val result = checkTransactions(view)
                if(result != null){
                    activity?.runOnUiThread { adapter.addItems(result) }
                }

                activity?.runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        adapter.addOnItemClickedCallback {
            val action = ReceiveInfoFragmentDirections.actionReceiveInfoFragmentToReceiveGestureFragment(it.transactionId,it.payer,it.amount)
            findNavController().navigate(action)
        }

        checkingJob = GlobalScope.launch {
            while (true){
                activity?.runOnUiThread {
                    swipeRefreshLayout.isRefreshing = true
                }
                val result = checkTransactions(view)

                if(result != null){
                    activity?.runOnUiThread { adapter.addItems(result) }
                }

                activity?.runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                }

                delay(60000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        checkingJob?.cancel()
    }

    private suspend fun checkTransactions(view:View):ArrayList<Transaction>? {
        try {
            return AWSApiCaller.checkTransactions(Amplify.Auth.currentUser.username)
        }catch(e:Exception){
            Snackbar.make(view,e.message.toString().filterNot{ it == '\"' },Snackbar.LENGTH_SHORT).show()
            Log.d("ReceiveInfoFragment",e.message.toString().filterNot{ it == '\"' })
        }
        return null
    }
}