package com.example.sduiassignment.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.databinding.ItemProductCardBinding
import com.example.sduiassignment.ui.common.ActionHandler

class ProductCardAdapter(
    private val items: List<WidgetPayload.ProductCollection.ProductItem>
) : RecyclerView.Adapter<ProductCardAdapter.VH>() {

    inner class VH(val binding: ItemProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding

        Glide.with(b.ivProductImage)
            .load(item.productImage?.url)
            .centerCrop()
            .into(b.ivProductImage)

        b.tvProductTag.text = item.tag?.text.orEmpty()
        b.tvProductTag.visibility = if (item.tag?.text.isNullOrBlank()) View.GONE else View.VISIBLE

        b.tvProductTitle.text = item.title.orEmpty()

        b.tvProductSubtitle.text = item.subtitle.orEmpty()
        b.tvProductSubtitle.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE

        b.tvProductSpecs.text = item.specs?.joinToString(" • ").orEmpty()

        b.tvProductPrice.text = item.price?.displayPrice.orEmpty()

        val emi = listOfNotNull(item.price?.emiText, item.price?.footnote).joinToString(" ")
        b.tvProductEmi.text = emi
        b.tvProductEmi.visibility = if (emi.isBlank()) View.GONE else View.VISIBLE

        val badgeText = item.badges?.joinToString("   ") { "✓ ${it.text.orEmpty()}" }.orEmpty()
        b.tvProductBadges.text = badgeText
        b.tvProductBadges.visibility = if (badgeText.isBlank()) View.GONE else View.VISIBLE

        var wishlisted = item.wishlistIcon?.selected == true
        b.ivWishlist.setImageResource(starResource(wishlisted))

        holder.itemView.setOnClickListener {
            ActionHandler.handle(holder.itemView.context, item.action, item.title)
        }
        b.ivWishlist.setOnClickListener {
            wishlisted = !wishlisted
            b.ivWishlist.setImageResource(starResource(wishlisted))
        }
    }

    private fun starResource(filled: Boolean): Int =
        if (filled) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off

    override fun getItemCount(): Int = items.size
}
