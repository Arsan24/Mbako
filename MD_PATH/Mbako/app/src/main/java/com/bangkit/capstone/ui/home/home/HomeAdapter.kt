package com.bangkit.capstone.ui.home.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.capstone.Constanta
import com.bangkit.capstone.Helper
import com.bangkit.capstone.R
import com.bangkit.capstone.data.model.DataItems
import com.bangkit.capstone.databinding.ItemTembakauBinding
import com.bangkit.capstone.ui.home.detail.DetailActivity
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

class HomeAdapter: PagingDataAdapter<DataItems, HomeAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTembakauBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: ItemTembakauBinding) :
        RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(items: DataItems){
                with(binding){
                    Glide.with(itemView)
                        .load(items.image)
                        .into(itemsImage)
                    tvJenisTembakau.text = items.pname
                    tvStartBid.text = "Rp.${NumberFormat.getNumberInstance(Locale("id", "ID")).format(items.price)}"
                    tvStok.text = "Stok: ${items.quantity}"
                    itemView.setOnClickListener {
                        val intent = Intent(itemView.context, DetailActivity::class.java)
                        intent.putExtra(Constanta.itemsDetail.UserProductName.name, items.pname)
                        intent.putExtra(Constanta.itemsDetail.UserPrice.name, items.price)
                        intent.putExtra(Constanta.itemsDetail.UserQuantity.name, items.quantity)
                        intent.putExtra(Constanta.itemsDetail.ImageUrl.name, items.image)
                        intent.putExtra(Constanta.itemsDetail.ItemId.name, items.item_id)
                        intent.putExtra(
                            Constanta.itemsDetail.UploadTime.name,
                            "${itemView.context.getString(R.string.const_text_uploaded)} ${
                                itemView.context.getString(
                                    R.string.const_text_time_on
                                )
                            } ${Helper.getUploadItemsTime(items.createdAt)}"
                        )

                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        val optionsCompat: ActivityOptionsCompat =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                itemView.context as Activity,
                                androidx.core.util.Pair(itemsImage, "items_image"),
                                androidx.core.util.Pair(tvJenisTembakau, "tv_jenis_tembakau"),
                                androidx.core.util.Pair(tvStok, "tv_stok"),
                                androidx.core.util.Pair(tvStartBid, "tv_start_bid"),
                            )
                        itemView.context.startActivity(intent, optionsCompat.toBundle())
                    }

                    if (items.quantity == 0) {
                        itemView.visibility = View.GONE
                    } else {
                        itemView.visibility = View.VISIBLE
                    }
                }
            }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItems>() {
            override fun areItemsTheSame(oldItem: DataItems, newItem: DataItems): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DataItems, newItem: DataItems): Boolean {
                return oldItem.item_id == newItem.item_id
            }
        }
    }
}