package com.example.sduiassignment.ui.home.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sduiassignment.data.model.GridCardStyle
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemGridCategoryBinding
import com.example.sduiassignment.databinding.ItemGridCategoryCardBinding
import com.example.sduiassignment.ui.common.ActionHandler
import com.example.sduiassignment.ui.common.parseHexColorOrNull

private const val VIEW_TYPE_ICON = 0
private const val VIEW_TYPE_IMAGE_CARD = 1

class GridItemAdapter(
    private val items: List<WidgetPayload.CategoryGrid.GridItem>,
    private val cardStyle: String? = GridCardStyle.ICON
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int =
        if (cardStyle == GridCardStyle.IMAGE_CARD) VIEW_TYPE_IMAGE_CARD else VIEW_TYPE_ICON

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_IMAGE_CARD) {
            ImageCardVH(ItemGridCategoryCardBinding.inflate(inflater, parent, false))
        } else {
            IconVH(ItemGridCategoryBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is IconVH -> holder.bind(item)
            is ImageCardVH -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class IconVH(val binding: ItemGridCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WidgetPayload.CategoryGrid.GridItem) {
            binding.tvGridItemTitle.text = item.title.orEmpty()

            val color = parseHexColorOrNull(item.backgroundColor)
            if (color != null) {
                (itemView.background.mutate() as? GradientDrawable)?.setColor(color)
            }

            Glide.with(binding.ivGridImage)
                .load(item.categoryImage?.url)
                .centerCrop()
                .into(binding.ivGridImage)
            itemView.setOnClickListener {
                ActionHandler.handle(itemView.context, item.action, item.title)
            }
        }
    }

    class ImageCardVH(val binding: ItemGridCategoryCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WidgetPayload.CategoryGrid.GridItem) {
            binding.tvCardTitle.text = item.title.orEmpty()
            binding.tvCardSubtitle.text = item.subtitle.orEmpty()
            binding.tvCardSubtitle.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE

            val color = parseHexColorOrNull(item.backgroundColor)
            if (color != null) {
                (itemView.background.mutate() as? GradientDrawable)?.setColor(color)
            }

            Glide.with(binding.ivCardImage)
                .load(item.categoryImage?.url)
                .centerCrop()
                .into(binding.ivCardImage)

            itemView.setOnClickListener {
                ActionHandler.handle(itemView.context, item.action, item.title)
            }
        }
    }
}
