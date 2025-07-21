    package com.example.khedutg.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.example.khedutg.CartListener
import com.example.khedutg.adapters.AdapterCartProducts
import com.example.khedutg.databinding.ActivityMainBinding
import com.example.khedutg.databinding.BsCartProductBinding
import com.example.khedutg.roomdb.CartProducts
import com.example.khedutg.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

    class UsersMainActivity : AppCompatActivity() , CartListener {
        private lateinit var binding: ActivityMainBinding
        private val viewModel : UserViewModel by viewModels()
        private lateinit var cartProductList: List<CartProducts>
        private lateinit var adapterCartProducts: AdapterCartProducts
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            getAllCartProducts()
            getTotalItemCountInCart()
            onCartClicked()
            onNextButtonClicked()

        }

        private fun onNextButtonClicked() {
            binding.btnNext.setOnClickListener {
                startActivity(Intent(this, OrderPlaceActivity::class.java))
            }
        }

        private fun onCartClicked() {
            binding.llItemCart.setOnClickListener {
                var bsCartProductBinding = BsCartProductBinding.inflate(LayoutInflater.from(this))
                bsCartProductBinding.btnNext.setOnClickListener {
                    startActivity(Intent(this, OrderPlaceActivity::class.java))
                }

                val bs = BottomSheetDialog(this)
                bs.setContentView(bsCartProductBinding.root)

                bsCartProductBinding.tvNumaberOfProductCount.text = binding.tvNumaberOfProductCount.text

                adapterCartProducts = AdapterCartProducts()
                bsCartProductBinding.rvProductsItems.adapter = adapterCartProducts
                adapterCartProducts.differ.submitList(cartProductList)

                bs.show()
            }
        }

        private fun getAllCartProducts() {
            viewModel.getAll().observe(this){
                cartProductList = it
            }
        }

        private fun getTotalItemCountInCart() {
            viewModel.fetchTotalCartItemCount().observe(this){
                if (it > 0){
                    binding.llCart.visibility= View.VISIBLE
                    binding.tvNumaberOfProductCount.text = it.toString()
                }else{
                    binding.llCart.visibility= View.GONE
                }
            }
        }

        override fun showCartLayout(itemCount : Int) {
            val previousCount = binding.tvNumaberOfProductCount.text.toString().toInt()
            val updatedCount = previousCount + itemCount

            if (updatedCount > 0){
                binding.llCart.visibility = View.VISIBLE
                binding.tvNumaberOfProductCount.text = updatedCount.toString()
            }
            else{
                binding.llCart.visibility = View.GONE
                binding.tvNumaberOfProductCount.text = "0"
            }
        }

        override fun onCartUpdated() {
            binding.llCart.visibility = View.VISIBLE

        }

        override fun savingCartItemCount(itemCount: Int) {
            viewModel.fetchTotalCartItemCount().observe(this){
                viewModel.savingCartItemCount(it + itemCount)
            }
        }

        override fun updateCartLayoutVisibility(totalCount: Int) {
            if (totalCount > 0) {
                binding.llCart.visibility = View.VISIBLE
            } else {
                binding.llCart.visibility = View.GONE
            }
        }

        override fun hideCartLayout() {
            binding.llCart.visibility = View.GONE
            binding.tvNumaberOfProductCount.text = "0"
        }

    }