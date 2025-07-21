package com.example.khedutg.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.khedutg.CartListener
import com.example.khedutg.roomdb.CartProducts
import com.example.khedutg.R
import com.example.khedutg.Utils
import com.example.khedutg.adapter.AdapterProduct
import com.example.khedutg.databinding.FragmentCategoryBinding
import com.example.khedutg.databinding.ItemViewProductBinding
import com.example.khedutg.model.Product
import com.example.khedutg.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import kotlin.String

class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    private var category: String? = null
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterProduct: AdapterProduct
    private var cartListener: CartListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        setStatusBarColor()
        getProductCategory()
        setToolBarTitle()
        onNavigationIconClick()
        fetchCategoryProduct()
        onSearchMenuClick()

        return binding.root
    }

    private fun onNavigationIconClick() {
        binding.tbSearchFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.tbSearchFragment.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchMenu -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun fetchCategoryProduct() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        category?.let { nonNullCategory ->
        lifecycleScope.launch {
            viewModel.getCategoryProduct(category!!).collect {
                if (it.isEmpty()) {
                    binding.rvProduct.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                } else {
                    binding.rvProduct.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(
                    ::onAddButtonClicked,
                    ::onIncrementButtonClicked,
                    ::onDecrementButtonClicked
                )
                binding.rvProduct.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.orginalList = it as ArrayList<Product>

                binding.shimmerViewContainer.visibility = View.GONE
            }
        } ?: run {
            Utils.showToast(requireContext(), "Category not found!")
            }
        }

    }

    private fun setToolBarTitle() {
        binding.tbSearchFragment.title = category
    }

    private fun getProductCategory() {
        val bundle = arguments
        category = bundle?.getString("category")
    }

    private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding) {
        productBinding.tvAdd.visibility = View.GONE
        productBinding.llProductCount.visibility = View.VISIBLE

        //step 1,

        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)


        // step 2
        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product, itemCount)
        }

    }

    private fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if (product.stock!! + 1 > itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            //step2,
            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product, itemCountInc)
            }
        }
        else{
            Utils.showToast(requireContext(), message = "Can,t add more item of this")
        }
    }

    fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product, itemCountDec)
        }

        if (itemCountDec >0){
            productBinding.tvProductCount.text = itemCountDec.toString()
        }
        else{
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
            productBinding.tvProductCount.text = "0"
        }

        cartListener?.showCartLayout(-1)

        //step2,
    }

    private fun saveProductInRoomDb(product: Product) {

        val cartProduct = CartProducts(
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle,
            productType = product.productType,
            productCategory = product.productCategory,
            productUnit = product.productUnit,
            productStock = product.stock,
            productImage = product.productImage?.get(0)!!,
            productCount = product.itemCount,
            productPrice = "₹" + "${product.price}",
            adminUid = product.adminUid
        )

        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)
        }
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.greenbox)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        if (context is CartListener){
            cartListener = context
        }else{
            throw  ClassCastException("Please implement cat listener")
        }
    }

}
