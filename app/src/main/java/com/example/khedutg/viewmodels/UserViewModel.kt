package com.example.khedutg.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.khedutg.Utils
import com.example.khedutg.model.Product
import com.example.khedutg.models.Bestseller
import com.example.khedutg.models.Orders
import com.example.khedutg.models.Users
import com.example.khedutg.roomdb.CartProductDao
import com.example.khedutg.roomdb.CartProducts
import com.example.khedutg.roomdb.CartProductsDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Address


class UserViewModel(application: Application) : AndroidViewModel(application) {

    //Initialization
    val sharedPreferences: SharedPreferences = application.getSharedPreferences("My_pref",  Context.MODE_PRIVATE)
    val cartProductDao : CartProductDao = CartProductsDatabase.getDatabaseInstance(application).cartProductDao()

    // Room DB
    suspend fun insertCartProduct(products: CartProducts){
        cartProductDao.insertCartProduct(products)
    }

    fun getAll(): LiveData<List<CartProducts>>{
        return cartProductDao.getAllCartProducts()
    }

    suspend fun deleteCartProducts(){
        cartProductDao.deleteCartProducts()
    }

    suspend fun updateCartProduct(products: CartProducts){
        cartProductDao.updateCartProduct(products)
    }

    suspend fun  deleteCartProduct(productId : String){
        cartProductDao.deleteCartProduct(productId)
    }
    //Firebase call
    fun fetchAllTheProducts(): Flow<List<Product>> = callbackFlow {
        val dbRef = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (productSnapshot in snapshot.children) {
                    val prod = productSnapshot.getValue(Product::class.java)
                    prod?.let { products.add(it) }
                }
                trySend(products).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())             }
        }

        dbRef.addValueEventListener(eventListener)

        awaitClose {
            dbRef.removeEventListener(eventListener)
        }
    }

    fun getAllOrders() : Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children) {
                    val order = orders.getValue(com.example.khedutg.models.Orders::class.java)
                    if (order != null && order.userId == Utils.getCurrentUserUid()) {
                        orderList.add(order)
                    }
                }
                trySend(orderList).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }

    fun getOrderedProducts(orderId : String) : Flow<List<CartProducts>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {
        val dbRef = FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory")
            .child(category)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (productSnapshot in snapshot.children) {
                    val prod = productSnapshot.getValue(Product::class.java)
                    prod?.let { products.add(it) }
                }
                trySend(products).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(eventListener)

        awaitClose {
            dbRef.removeEventListener(eventListener)
        }
    }

    fun getUserAddress(callback : (String?) -> Unit){
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserUid()
            .toString()).child("userAddress")

        db.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                }
                else{
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)

            }

        })

    }

    fun updateItemCount(product : Product, itemCount: Int){
        val dbRef = FirebaseDatabase.getInstance().getReference("Admins")
        dbRef.child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        dbRef.child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        dbRef.child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)
    }

    fun saveProductsAfterOrder(stock : Int, product: CartProducts){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("productStock").setValue(stock)
    }

    fun saveUserAddress(address : String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserUid()
            .toString()).child("userAddress").setValue(address)
    }

    fun saveAddress(address: String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserUid()
            .toString()).child("userAddress").setValue(address)
    }

    fun saveUserName(name: String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserUid()
            .toString()).child("userName").setValue(name)
    }
    fun saveOrderedProducts(order: Orders, callback: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Orders")
            .child(order.orderId!!)
            .setValue(order)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit()
            .putInt("itemCount", itemCount.coerceAtLeast(0))
            .apply()
    }

    fun fetchTotalCartItemCount(): LiveData<Int> {
        val count = sharedPreferences.getInt("itemCount", 0)
        val liveData = MutableLiveData<Int>()
        liveData.value = count
        return liveData
    }

    fun saveAddressStatus(){
        sharedPreferences.edit().putBoolean("adressStatus", true).apply()
    }

    fun getUserProfile(callback: (Users?) -> Unit) {
        val uid = Utils.getCurrentUserUid().toString()
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(uid)

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun fetchProductType() : Flow<List<Bestseller>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins/ProductType")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productTypeList = ArrayList<Bestseller>()
                for (productType in snapshot.children){
                    val productTypeName = productType.key

                    val productList = ArrayList<Product>()

                    for (products in productType.children){
                        val product = products.getValue(Product::class.java)
                        productList.add(product!!)
                    }
                    val bestseller = Bestseller(productType = productTypeName, products = productList)
                    productTypeList.add(bestseller)
                }
                trySend(productTypeList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }

}