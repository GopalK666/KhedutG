package com.example.khedutg.model

import java.util.UUID

data class Product(
    var productRandomId: String? = null,
    var productTitle: String? = null,
    var productCategory: String? = null,
    var productUnit: String? = null,
    var productType: String? = null,
    var price: Double? = null,
    var itemCount: Int? = null,
    var stock: Int? = null,
    var adminUid: String? = null,
    var productImage: List<String>? = null
)
