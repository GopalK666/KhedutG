            package com.example.khedutg.activity

            import android.content.Intent
            import android.os.Build
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import org.json.JSONObject
            import com.razorpay.Checkout
            import androidx.appcompat.app.AppCompatActivity
            import androidx.appcompat.app.AlertDialog
            import androidx.activity.viewModels
            import androidx.core.content.ContextCompat
            import com.razorpay.PaymentResultListener
            import androidx.lifecycle.lifecycleScope
            import com.example.khedutg.CartListener
            import com.example.khedutg.R
            import com.example.khedutg.Utils
            import com.example.khedutg.adapters.AdapterCartProducts
            import com.example.khedutg.viewmodels.UserViewModel
            import com.example.khedutg.databinding.ActivityOrderPlaceBinding
            import com.example.khedutg.databinding.AddressLayoutBinding
            import com.example.khedutg.models.Orders
            import kotlinx.coroutines.launch


            class OrderPlaceActivity : AppCompatActivity(), PaymentResultListener {
                private lateinit var binding: ActivityOrderPlaceBinding
                private val viewModel: UserViewModel by viewModels()
                private lateinit var adapterCartProducts: AdapterCartProducts
                private var isAddressSaved = false
                private var selectedPaymentMethod: String? = null
                private var cartListener : CartListener?=null


                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
                    setContentView(binding.root)

                    val rbOnline = binding.rbOnline
                    val rbCOD = binding.rbCOD
                    val paymentMethodTextView = binding.tvPaymentMethod

                    getAllCartProducts()
                    checkSavedAddress()
                    setStatusBarColor()
                    backToUserMainActivity()
                    onPlaceOrderClicked()

                    // Payment method radio buttons listener
                    rbOnline.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedPaymentMethod = "Online Payment"
                            paymentMethodTextView.text = "Online Payment"
                            paymentMethodTextView.visibility = View.VISIBLE
                        }
                    }

                    rbCOD.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedPaymentMethod = "Cash on Delivery"
                            paymentMethodTextView.text = "Cash on Delivery"
                            paymentMethodTextView.visibility = View.VISIBLE
                        }
                    }

                }

                //Order ne save karva mate firebase ma
                private fun saveOrder(paymentType: String, paymentId: String?) {
                    viewModel.getAll().observe(this@OrderPlaceActivity) { cartProductList ->
                        if (cartProductList.isNotEmpty()){
                            val userId = Utils.getCurrentUserUid().toString()
                            val orderId = System.currentTimeMillis().toString()
                            val totalAmount = binding.tvGrandTotal.text.toString().replace("₹", "").toDoubleOrNull() ?: 0.0
                            val currentDate = Utils.getCurrentDate()

                            viewModel.getUserAddress { address ->

                                viewModel.getUserProfile { user ->
                                    val order = Orders(
                                        orderId = orderId,
                                        userId = userId,
                                        userName = user?.userName,
                                        userPhone = user?.userPhoneNumber,
                                        userAddress = address,
                                        orderList = cartProductList,
                                        totalAmount = totalAmount,
                                        orderDate = currentDate,
                                        paymentMethod = paymentType,
                                        paymentId = paymentId,
                                        orderStatus = 0
                                    )

                                    viewModel.saveOrderedProducts(order) { success ->
                                        if (success) {
                                            lifecycleScope.launch {
                                                viewModel.deleteCartProducts()
                                                viewModel.savingCartItemCount(0)
                                                cartListener?.hideCartLayout()
                                            }

                                            Utils.showToast(this@OrderPlaceActivity, "Order placed successfully!")
                                            startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                                            finish()
                                        } else {
                                            Utils.showToast(this@OrderPlaceActivity, "Failed to place order.")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //payment popopup khule
                private fun startRazorpayPayment() {
                    val checkout = Checkout()
                    checkout.setKeyID("xxxxxxxxxx") //Key RazorPay se

                    try {
                        val options = JSONObject()
                        options.put("name", "KhedutG")
                        options.put("description", "Product Charges")
                        options.put("theme.color", "#3399cc")
                        options.put("currency", "INR")

                        // Amount in paise (e.g., ₹200 = 20000)
                        val grandTotal = binding.tvGrandTotal.text.toString().replace("₹", "").toDouble()
                        options.put("amount", (grandTotal * 100).toInt()) // amount in paisa

                        val prefill = JSONObject()
                        prefill.put("email", "gopalkamaliya@gmail.com") // optional
                            prefill.put("contact", "6666666666")      // optional

                        options.put("prefill", prefill)

                        checkout.open(this, options)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Utils.showToast(this, "Error in payment: ${e.message}")
                    }
                }

                private fun checkSavedAddress() {
                    lifecycleScope.launch {
                        viewModel.getUserAddress { address ->
                            if (!address.isNullOrEmpty()) {
                                isAddressSaved = true
                                binding.llpayment.visibility = View.VISIBLE
                            } else {
                                isAddressSaved = false
                                binding.llpayment.visibility = View.GONE
                            }
                        }
                    }
                }
                //Place order button clicked
                private fun onPlaceOrderClicked() {
                    binding.llPlaceOrder.setOnClickListener {
                        lifecycleScope.launch {
                            viewModel.getUserAddress { address ->

                                if (address.isNullOrEmpty()) {
                                    // Show Address Dialog
                                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this@OrderPlaceActivity))
                                    val alertDialog = AlertDialog.Builder(this@OrderPlaceActivity)
                                        .setView(addressLayoutBinding.root)
                                        .create()
                                    alertDialog.show()

                                    addressLayoutBinding.btnAdd.setOnClickListener {
                                        saveAddress(alertDialog, addressLayoutBinding)
                                    }
                                } else {
                                    if (selectedPaymentMethod.isNullOrEmpty()) {
                                        Utils.showToast(this@OrderPlaceActivity, "Please select payment method")
                                        return@getUserAddress
                                    }

                                    if (selectedPaymentMethod == "Online Payment") {
                                        startRazorpayPayment() //Razorpay CALL HERE
                                    } else {
                                        Utils.showToast(this@OrderPlaceActivity, "Order placed with $selectedPaymentMethod")
                                        saveOrder("Cash on Delivery", null)
                                    }
                                }
                            }
                        }
                    }
                }

                private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
                    Utils.showDialog(this, "Processing...")

                    val fullName = addressLayoutBinding.etFullName.text.toString().trim()
                    val phoneNumber = addressLayoutBinding.etPhoneNumber.text.toString().trim()
                    val alternatePhone = addressLayoutBinding.etAlternatePhone.text.toString().trim()
                    val houseArea = addressLayoutBinding.etHouseArea.text.toString().trim()
                    val landmark = addressLayoutBinding.etLandmark.text.toString().trim()
                    val pinCode = addressLayoutBinding.etPinCode.text.toString().trim()
                    val state = addressLayoutBinding.etState.text.toString().trim()
                    val district = addressLayoutBinding.etDistrict.text.toString().trim()
                    val city = addressLayoutBinding.etCity.text.toString().trim()

                    val address = "$houseArea,$landmark,$city,$district, $state - $pinCode Phone: $phoneNumber $alternatePhone"


                    lifecycleScope.launch {
                    viewModel.saveUserAddress(address)
                    viewModel.saveUserName(fullName)
                    viewModel.saveAddressStatus()

                    isAddressSaved = true

                    binding.llItemCart.visibility = View.VISIBLE
                    binding.llpayment.visibility = View.VISIBLE
                }
                Utils.showToast(this, "Saved..")
                alertDialog.dismiss()
                Utils.hideDialog()
            }

                private fun backToUserMainActivity() {
                    binding.tbOrderFragment.setNavigationOnClickListener {
                        startActivity(Intent(this, UsersMainActivity::class.java))
                        finish()
                    }
                }

                private fun getAllCartProducts() {
                    viewModel.getAll().observe(this){cartProductList->
                        adapterCartProducts = AdapterCartProducts()
                        binding.rvProductsItems.adapter = adapterCartProducts
                        adapterCartProducts.differ.submitList(cartProductList)

                        var totalPrice = 0.0

                        for (products in cartProductList){
                            val priceStr = products.productPrice?.substring(1)
                            val price = priceStr?.toDoubleOrNull() ?: 0.0

                            val itemCount = products.productCount!!
                            totalPrice += (price?. times(itemCount)!!)

                        }
                        binding.tvSubTotal.text = "₹${totalPrice.toInt()}"

                        if (totalPrice > 200){
                            binding.tvDeliveryCharge.text = "₹20"
                            totalPrice +=20
                        }else{
                            binding.tvDeliveryCharge.text ="₹50"
                            totalPrice +=50
                        }
                        binding.tvGrandTotal.text = "₹${totalPrice.toInt()}"
                    }
                }

                private fun setStatusBarColor() {
                        window?.apply {
                        val statusBarColors = ContextCompat.getColor(this@OrderPlaceActivity, R.color.greenbox)
                        statusBarColor = statusBarColors
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    }
                }

                override fun onPaymentSuccess(razorpayPaymentID: String?) {
                    Utils.showToast(this, "Payment Successful")
                    saveOrder("Online Payment", razorpayPaymentID)
                }

                override fun onPaymentError(code: Int, response: String?) {
                    Utils.showToast(this, "Payment Failed")
                }
            }
