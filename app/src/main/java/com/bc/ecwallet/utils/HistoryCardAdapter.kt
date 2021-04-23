package com.bc.ecwallet.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bc.ecwallet.R
import java.text.SimpleDateFormat

class HistoryCardAdapter():RecyclerView.Adapter<HistoryCardAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val textView: TextView
        val img_card_logo:ImageView
        val txt_card_detail:TextView
        val txt_card_time:TextView
        val txt_card_amount:TextView

        init {
            // Define click listener for the ViewHolder's View.
            //textView = view.findViewById(R.id.textView)
            img_card_logo=view.findViewById(R.id.img_card_logo)
            txt_card_detail=view.findViewById(R.id.txt_card_detail)
            txt_card_time=view.findViewById(R.id.txt_card_time)
            txt_card_amount=view.findViewById(R.id.txt_card_amount)
        }

    }

    private val dataSet: ArrayList<PaymentHistory> = arrayListOf()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.cardview_history, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        //viewHolder.textView.text = dataSet[position]

        if(dataSet[position].type == PaymentHistory.PAYING){
            viewHolder.img_card_logo.setImageResource(R.drawable.ic_pay)
            viewHolder.txt_card_detail.text="Paid to "+dataSet[position].target
        }else{
            viewHolder.img_card_logo.setImageResource(R.drawable.ic_receive)
            viewHolder.txt_card_detail.text="Received from "+dataSet[position].target
        }

        viewHolder.txt_card_amount.text="$"+"%.2f".format(dataSet[position].amount)
        val date = dataSet[position].time.time
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        viewHolder.txt_card_time.text = format.format(date)
        //viewHolder.txt_card_time.text = dataSet
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun addItem(record:PaymentHistory){
        dataSet.add(record)
        notifyDataSetChanged()
    }

    fun addItems(records:ArrayList<PaymentHistory>){
        val newRecords = records.filterNot { dataSet.contains(it) }
        dataSet.addAll(newRecords)
        notifyDataSetChanged()
    }

}