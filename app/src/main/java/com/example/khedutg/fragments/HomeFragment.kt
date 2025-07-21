    package com.example.khedutg.fragments

    import android.content.Context
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.GridLayoutManager
    import com.example.khedutg.CartListener
    import com.example.khedutg.Constants
    import com.example.khedutg.R
    import com.example.khedutg.Utils
    import com.example.khedutg.adapter.AdapterProduct
    import com.example.khedutg.adapters.AdapterBestseller
    import com.example.khedutg.adapters.AdapterCategory
    import com.example.khedutg.adapters.GridSpacingItemDecoration
    import com.example.khedutg.databinding.BgSellAllBinding
    import com.example.khedutg.databinding.FragmentHomeBinding
    import com.example.khedutg.databinding.ItemViewProductBinding
    import com.example.khedutg.model.Product
    import com.example.khedutg.models.Bestseller
    import com.example.khedutg.models.Category
    import com.example.khedutg.roomdb.CartProducts
    import com.example.khedutg.viewmodels.UserViewModel
    import com.google.android.material.bottomsheet.BottomSheetDialog
    import kotlinx.coroutines.launch


    class HomeFragment : Fragment() {

        private  lateinit var  binding: FragmentHomeBinding
        private val viewModel : UserViewModel by viewModels()
        private lateinit var adapterBestseller: AdapterBestseller
        private lateinit var adapterProduct: AdapterProduct
        private var cartListener : CartListener ?= null
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentHomeBinding.inflate(layoutInflater)
            // Inflate the layout for this fragment
            setStatusBarColor()
            setAllCategories()
            navigationToSearchFragment()
            onProfileClicked()

            fetchBestseller()
            return binding.root
            return inflater.inflate(R.layout.fragment_home, container, false)
        }

        private fun fetchBestseller() {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            lifecycleScope.launch {
                viewModel.fetchProductType().collect{
                    adapterBestseller = AdapterBestseller(::onSellAllButtonClicked)
                    binding.rvBestsellers.adapter = adapterBestseller
                    adapterBestseller.differ.submitList(it)
                    binding.shimmerViewContainer.visibility = View.GONE
                }
            }
        }

        private fun onProfileClicked() {
            binding.ivProfile.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }
        }


        private fun navigationToSearchFragment() {
            binding.searchCv.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
            }
        }

        private fun setAllCategories() {
            val categoryList = ArrayList<Category>()

            for (i in 0 until Constants.allProductsCategoryIcon.size) {
                categoryList.add(Category(Constants.allProductsCategory[i], Constants.allProductsCategoryIcon[i]))
            }

            binding.rvCategories.adapter = AdapterCategory(categoryList, ::OnCategoryIconClicked)
        }
        fun OnCategoryIconClicked(category: Category){
            val bundle = Bundle()
            bundle.putString("category", category.title)
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
        }

        fun onSellAllButtonClicked(productType : Bestseller){
            val bgSellAllBinding = BgSellAllBinding.inflate(LayoutInflater.from(requireContext()))
            val bs = BottomSheetDialog(requireContext())
            bs.setContentView(bgSellAllBinding.root)

            adapterProduct = AdapterProduct(::onAddButtonClicked, ::onIncrementButtonClicked, ::onDecrementButtonClicked)
            bgSellAllBinding.rvProduct.adapter = adapterProduct
            adapterProduct.differ.submitList(productType.products)

            bs.show()
        }
        private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding){
            productBinding.tvAdd.visibility = View.GONE
            productBinding.llProductCount.visibility = View.VISIBLE

            //step 1,

            var itemCount = productBinding.tvProductCount.text.toString().toInt()
            itemCount++
            productBinding.tvProductCount.text = itemCount.toString()

            cartListener?.showCartLayout(1)


            //step2,
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            if (context is CartListener){
                cartListener = context
            }
            else{
                throw  ClassCastException("Please implement cart listener")
            }
        }
    }