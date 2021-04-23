package com.bc.ecwallet.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bc.ecwallet.R


class DefaultViewRecyclerAdapter(val context:Context?): RecyclerView.Adapter<DefaultViewRecyclerAdapter.ViewHolder>() {
    private var itemList : ArrayList<ListArgs> = arrayListOf()
    var onItemClickListener: ((String)->Unit)? = null

    class ViewHolder(view: View,onItemClickListener:((String)->Unit)?):RecyclerView.ViewHolder(view) {
        val img_logo:ImageView = view.findViewById(R.id.img_logo)
        val txt_title:TextView = view.findViewById(R.id.txt_title)
        val layoutDefault = view.findViewById<LinearLayout>(R.id.layout_default)

        init{
            layoutDefault.setOnClickListener{
                onItemClickListener?.let {
                    it(txt_title.text.toString())
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_recycler_default, parent, false)

        return ViewHolder(
            view,
            onItemClickListener
        )
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemList?.let{
            holder.txt_title.text= it[position].title
            holder.img_logo.setImageResource(it[position].imgSrc)
        }
    }


    fun addItem(title: String,imgSrc: Int){
        itemList.add(
            ListArgs(
                title,
                imgSrc
            )
        )
        notifyDataSetChanged()
    }

    fun addOnItemClickListener(listener:((item:String)->Unit)){
        onItemClickListener = listener
    }

    class ListArgs(val title: String,val imgSrc: Int){}
}