package com.example.khedutg.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartProducts")
data class CartProducts (

    @PrimaryKey
    val productRandomId: String = "random",

    val productTitle: String? = null,
    var productCategory: String? = null,
    var productUnit: String? = null,
    var productType: String? = null,
    val productPrice: String ? = null,
    var productCount : Int ? = null,
    var productStock : Int ? = null,
    var adminUid: String? = null,
    var productImage: String? = null
)



