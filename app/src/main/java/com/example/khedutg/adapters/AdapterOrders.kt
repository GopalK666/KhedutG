package com.example.khedutg.adapters

import android.view.ViewGroup
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.khedutg.databinding.ItemViewOrdersBinding
import com.example.khedutg.R
import com.example.khedutg.models.OrderedItems



class AdapterOrders(val context: Context, val onOrderItemViewClicked: (OrderedItems) -> Unit): RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {
    class OrdersViewHolder(val binding: ItemViewOrdersBinding) : ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<OrderedItems>() {
        override fun areItemsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderedItems,newItem: OrderedItems): Boolean {
            return oldItem ==newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(ItemViewOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: OrdersViewHolder,position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            tvOrderTitles.text = order.itemTitle
            tvOrderDate.text = order.orderDate
            tvOrderAmount.text = "₹${order.itemPrice.toString()}"

            when(order.itemStatus){
                0 -> {
                    tvOrderStatus.text = "Ordered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.yellow)
                }
                1 -> {
                    tvOrderStatus.text = "Recevied"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue)
                }
                2 -> {
                    tvOrderStatus.text = "Dispatched"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                }
                3 -> {
                    tvOrderStatus.text = "Delivered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange)
                }
            }
        }
        holder.itemView.setOnClickListener {
            onOrderItemViewClicked(order)
        }
    }
}