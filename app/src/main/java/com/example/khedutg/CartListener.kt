package com.example.khedutg

interface CartListener {

    fun showCartLayout(itemCount: Int)

    fun onCartUpdated()

    fun savingCartItemCount(itemCount: Int)

    fun updateCartLayoutVisibility(totalCount: Int)

    fun hideCartLayout()

}