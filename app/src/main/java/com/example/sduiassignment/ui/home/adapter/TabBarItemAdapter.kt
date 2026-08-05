package com.example.sduiassignment.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sduiassignment.R
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemTabChipBinding
import com.example.sduiassignment.ui.common.ActionHandler

class TabBarItemAdapter(
    private val tabs: List<WidgetPayload.TabBar.TabItem>,
    private val onTabSelected: (WidgetPayload.TabBar.TabItem) -> Unit = {}
) : RecyclerView.Adapter<TabBarItemAdapter.VH>() {

    private var selectedPosition = tabs.indexOfFirst { it.selected == true }.coerceAtLeast(0)

    init {
        if (tabs.isNotEmpty()) onTabSelected(tabs[selectedPosition])
    }

    inner class VH(val binding: ItemTabChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTabChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tab = tabs[position]
        val label = holder.binding.tvTabLabel
        label.text = tab.label.orEmpty()
        applySelectionStyle(label, isSelected = position == selectedPosition)

        holder.itemView.setOnClickListener {
            if (selectedPosition == position) return@setOnClickListener
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onTabSelected(tab)
            ActionHandler.handle(holder.itemView.context, tab.action, tab.label)
        }
    }

    private fun applySelectionStyle(label: android.widget.TextView, isSelected: Boolean) {
        if (isSelected) {
            label.setBackgroundResource(R.drawable.bg_tab_selected)
            label.setTextColor(label.context.getColor(R.color.brand_purple_dark))
        } else {
            label.setBackgroundResource(R.drawable.bg_tab_unselected)
            label.setTextColor(label.context.getColor(R.color.text_secondary))
        }
    }

    override fun getItemCount(): Int = tabs.size
}
