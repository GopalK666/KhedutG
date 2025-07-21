package com.example.khedutg.models

import com.example.khedutg.model.Product

data class Bestseller(
    val id : String ? = null,
    val productType: String ? =null,
    val products : ArrayList<Product> ? = null
)