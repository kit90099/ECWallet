package com.bc.ecwallet.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bc.ecwallet.R

class PayeeInfoAdapter: RecyclerView.Adapter<PayeeInfoAdapter.ViewHolder>() {
    private val listPayee = arrayListOf<Payee>()
    var onItemClickedCallback:((String)->Unit)? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var txt_payee:TextView = view.findViewById(R.id.txt_payee)
        var txt_status:TextView = view.findViewById(R.id.txt_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_recycler_payee,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listPayee.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payee = listPayee[position]

        holder.txt_payee.text = payee.name
        when(payee.status){
            AVAILABLE-> holder.txt_status.text = "Available"
            NearbyPayer.FAILED-> holder.txt_status.text = "Failed"
            NearbyPayer.REJECTED-> holder.txt_status.text = "Rejected"
        }

        holder.view.setOnClickListener {
            holder.txt_status.text = "Connecting"

            onItemClickedCallback?.invoke(payee.name)
            }
    }

    fun addItem(payeeName:String){
        listPayee.add(Payee(payeeName,-1))
        notifyDataSetChanged()
    }

    fun addOnItemClickedCallback(callback:((String)->Unit)){
        onItemClickedCallback = callback
    }

    fun updateStatus(payee:String,status:Int){
        listPayee.iterator().forEach {
            if(it.name == payee){
                it.status = status
                notifyDataSetChanged()
            }
        }
    }

    private class Payee(var name:String,var status:Int){

    }

    companion object{
        const val AVAILABLE = 3
    }
}