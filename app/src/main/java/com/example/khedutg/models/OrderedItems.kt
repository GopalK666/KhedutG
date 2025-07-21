package com.example.khedutg.models

data class OrderedItems(
    val orderId : String? = null,
    val orderDate: String? = null,
    val itemStatus : Int?= null,
    val itemTitle : String?=null,
    val itemPrice : Int?=null
)