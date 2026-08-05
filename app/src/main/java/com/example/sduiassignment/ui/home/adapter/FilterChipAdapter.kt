package com.example.sduiassignment.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemFilterChipBinding
import com.example.sduiassignment.ui.common.ActionHandler

class FilterChipAdapter(
    private val chips: List<WidgetPayload.ProductCollection.FilterChip>
) : RecyclerView.Adapter<FilterChipAdapter.VH>() {

    private var selectedPosition = chips.indexOfFirst { it.selected == true }.coerceAtLeast(0)

    inner class VH(val binding: ItemFilterChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFilterChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chip = chips[position]
        holder.binding.chipFilter.text = chip.label.orEmpty()
        holder.binding.chipFilter.isChecked = position == selectedPosition
        holder.binding.chipFilter.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            ActionHandler.handle(holder.itemView.context, chip.action, chip.label)
        }
    }

    override fun getItemCount(): Int = chips.size
}
