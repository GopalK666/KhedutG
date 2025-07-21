package com.example.khedutg

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.khedutg.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Utils {

    private var dialog: AlertDialog? = null

    fun showDialog(context: Context, message: String) {
        val progressBinding = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        progressBinding.tvMessage.text = message

        dialog = AlertDialog.Builder(context)
            .setView(progressBinding.root)
            .setCancelable(false)
            .create()

        dialog?.show()
    }

    fun hideDialog() {
        dialog?.dismiss()
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private var firebaseAuthInstance: FirebaseAuth? = null

    fun getAuthInstance(): FirebaseAuth {
        if (firebaseAuthInstance == null) {
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return firebaseAuthInstance!!
    }

    fun getCurrentUserUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getRandomId() : String{
        return (1..25).map {
            (('A'..'Z') + ('a'..'z') + ('0'..'9')).random()
        }.joinToString("")
    }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
    object RetrofitInstance {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}