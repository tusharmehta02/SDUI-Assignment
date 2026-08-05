package com.example.sduiassignment.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemBannerPageBinding
import com.example.sduiassignment.ui.common.ActionHandler

class BannerPagerAdapter(
    private val items: List<WidgetPayload.BannerCarousel.BannerItem>
) : RecyclerView.Adapter<BannerPagerAdapter.VH>() {

    inner class VH(val binding: ItemBannerPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBannerPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        Glide.with(holder.binding.ivBannerImage)
            .load(item.image?.url)
            .centerCrop()
            .into(holder.binding.ivBannerImage)
        holder.itemView.setOnClickListener {
            ActionHandler.handle(holder.itemView.context, item.action, item.id)
        }
    }

    override fun getItemCount(): Int = items.size
}
