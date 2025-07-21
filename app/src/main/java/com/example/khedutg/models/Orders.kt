package com.example.khedutg.models

import androidx.appcompat.widget.DialogTitle
import com.example.khedutg.roomdb.CartProducts

data class Orders(
    val orderId: String? = null,
    val userId: String?  = null,
    val itemTitle: String? = null,
    val userName: String? = null,
    val itemPrice : Int? = null,
    val userPhone: String? = null,
    val userAddress: String? = null,
    val orderList: List<CartProducts>? = null,
    val totalAmount: Double? = null,
    val orderDate: String? = null,
    val paymentMethod: String? = null,
    val paymentId: String? = null,
    val orderStatus: Int? = 0,
)
