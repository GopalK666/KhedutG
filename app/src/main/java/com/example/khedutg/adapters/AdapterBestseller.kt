package com.example.khedutg.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.khedutg.databinding.ItemViewBestsellerBinding
import com.example.khedutg.models.Bestseller

class AdapterBestseller(
    private val onSellAllButtonClicked: (Bestseller) -> Unit
) : RecyclerView.Adapter<AdapterBestseller.BestsellerViewHolder>() {

    inner class BestsellerViewHolder(val binding: ItemViewBestsellerBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Bestseller>() {
        override fun areItemsTheSame(oldItem: Bestseller, newItem: Bestseller): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bestseller, newItem: Bestseller): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestsellerViewHolder {
        val binding = ItemViewBestsellerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BestsellerViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: BestsellerViewHolder, position: Int) {
        val productType = differ.currentList[position]
        val binding = holder.binding

        // Set title and product count
        binding.tvProductType.text = productType.productType
        binding.tvTotalProducts.text = "${productType.products?.size ?: 0} products"

        // Handle image previews
        val imageViews = listOf(binding.ivProduct1, binding.ivProduct2, binding.ivProduct3)

        // Reset visibility to GONE first
        imageViews.forEach { it.visibility = View.GONE }
        binding.tvProductCount.visibility = View.GONE

        val productList = productType.products ?: emptyList()
        val displayCount = minOf(imageViews.size, productList.size)

        for (i in 0 until displayCount) {
            val imageUrl = productList[i].productImage?.getOrNull(0)
            if (!imageUrl.isNullOrBlank()) {
                imageViews[i].visibility = View.VISIBLE
                Glide.with(holder.itemView)
                    .load(imageUrl)
                    .into(imageViews[i])
            }
        }

        if (productList.size > 3) {
            binding.tvProductCount.visibility = View.VISIBLE
            binding.tvProductCount.text = "+${productList.size - 3}"
        }

        holder.itemView.setOnClickListener {
            onSellAllButtonClicked(productType)
        }
    }
}
