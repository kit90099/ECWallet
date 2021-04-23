package com.bc.ecwallet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amplifyframework.core.Amplify
import com.bc.ecwallet.utils.AWSApiCaller
import com.bc.ecwallet.utils.HistoryCardAdapter
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

class OverviewFragment:Fragment() {
    val historyCardAdapter = HistoryCardAdapter()
    var jobChecking:Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val history= PaymentHistory(
            true,
            "tom",
            LocalDateTime.now(),
            100.00
        )*/

        view.findViewById<SwipeRefreshLayout>(R.id.layout_refresh).setOnRefreshListener {
            refresh(view)
            view.findViewById<SwipeRefreshLayout>(R.id.layout_refresh).isRefreshing = false
        }

        recycler_overview.layoutManager=LinearLayoutManager(activity)
        recycler_overview.adapter =historyCardAdapter

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = ""
        txt_welcome.text = "Welcome, "+sharedPref.getString("nickname", defaultValue)
    }

    override fun onResume() {
        super.onResume()

        jobChecking = GlobalScope.launch {
            val userName = Amplify.Auth.currentUser.username

            while(true) {
                val saving = AWSApiCaller.checkSavings(userName)
                view?.findViewById<TextView>(R.id.txt_overview_amount)?.text =String.format("$%.2f", saving)
                val histories = AWSApiCaller.getHistory(userName,10)
                histories?.let{
                    activity?.runOnUiThread { historyCardAdapter.addItems(it) }
                }

                delay(60000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        jobChecking?.cancel()
    }

    private fun refresh(view:View){
        val userName = Amplify.Auth.currentUser.username
        AWSApiCaller.checkSavings(userName) {
            view.findViewById<TextView>(R.id.txt_overview_amount).text = String.format("$%.2f",it)
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
            with (sharedPref?.edit()) {
                this?.putFloat("saving", it.toFloat())
                this?.apply()
            }
        }
        AWSApiCaller.getHistory(userName,10){
            historyCardAdapter.addItems(it)
        }
    }
}