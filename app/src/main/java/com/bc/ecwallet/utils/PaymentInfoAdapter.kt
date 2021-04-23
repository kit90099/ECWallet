package com.bc.ecwallet.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bc.ecwallet.R

class PaymentInfoAdapter:RecyclerView.Adapter<PaymentInfoAdapter.ViewHolder>()  {
    private var listTransaction = arrayListOf<Transaction>()
    var onItemClickedCallback:((Transaction)->Unit)? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var txt_payer: TextView = view.findViewById(R.id.txt_payee)
        var txt_amount: TextView = view.findViewById(R.id.txt_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_recycler_payment,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listTransaction.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = listTransaction[position]

        holder.txt_payer.text = "Receiving from " + transaction.payer
        holder.txt_amount.text = "$"+transaction.amount.toString()

        holder.view.setOnClickListener {

            onItemClickedCallback?.invoke(transaction)
        }
    }

    fun addItems(transactions: ArrayList<Transaction>){
        listTransaction = transactions
        notifyDataSetChanged()
    }

    fun addOnItemClickedCallback(callback:((Transaction)->Unit)){
        onItemClickedCallback = callback
    }

}