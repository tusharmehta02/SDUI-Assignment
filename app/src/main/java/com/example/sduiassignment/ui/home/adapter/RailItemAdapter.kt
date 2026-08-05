package com.example.sduiassignment.ui.home.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemRailCardBinding
import com.example.sduiassignment.ui.common.ActionHandler
import com.example.sduiassignment.ui.common.parseHexColorOrNull

class RailItemAdapter(
    private val items: List<WidgetPayload.HorizontalRail.RailItem>
) : RecyclerView.Adapter<RailItemAdapter.VH>() {

    inner class VH(val binding: ItemRailCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRailCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvRailItemTitle.text = item.title.orEmpty()

        val color = parseHexColorOrNull(item.backgroundColor)
        if (color != null) {
            (holder.itemView.background.mutate() as? GradientDrawable)?.setColor(color)
        }

        Glide.with(holder.binding.ivRailImage)
            .load(item.backgroundImage?.url)
            .centerCrop()
            .into(holder.binding.ivRailImage)
        holder.itemView.setOnClickListener {
            ActionHandler.handle(holder.itemView.context, item.action, item.title)
        }
    }

    override fun getItemCount(): Int = items.size
}
