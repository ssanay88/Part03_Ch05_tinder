package com.example.part03_ch05_tinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MatchedUserAdapter : ListAdapter<CardItem, MatchedUserAdapter.ViewHolder>(diffUtill) {


    // 뷰 바인딩을 사용하지 않고 구현
    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(cardItem: CardItem) {
            view.findViewById<TextView>(R.id.nameTextView).text = cardItem.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchedUserAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_matched_user , parent , false))
    }

    override fun onBindViewHolder(holder: MatchedUserAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtill = object : DiffUtil.ItemCallback<CardItem>() {
            override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem == newItem
            }

        }
    }


}