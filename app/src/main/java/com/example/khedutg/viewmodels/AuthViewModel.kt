package com.example.khedutg.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.khedutg.Utils
import com.example.khedutg.models.Users
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _otpSent = MutableStateFlow(false) // Default false

    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser = _isCurrentUser

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isCurrentUser.value = true
        }
    }


    fun sendOTP(userNumber: String, activity: Activity) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: FirebaseException) {}

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _verificationId.value = verificationId
                _otpSent.value = true
            }
        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance()) //Fixed here
            .setPhoneNumber("+91$userNumber") // Phone number to verify
            .setTimeout(60, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity for callback binding
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, context: Context) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)

        Utils.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = Utils.getCurrentUserUid()
                    if (uid != null) {
                        val user = Users(uid = uid, userPhoneNumber = userNumber, userAddress = null)
                        FirebaseDatabase.getInstance()
                            .getReference("AllUsers")
                            .child("Users")
                            .child(uid)
                            .setValue(user)
                        _isSignedInSuccessfully.value = true
                    } else {
                        Utils.showToast(context, "User UID is null!")
                    }
                } else {
                    Utils.showToast(context, "Sign-in failed: ${task.exception?.message}")
                }
            }
    }
    fun saveUserToFirestore(user: Users) {
        FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(user.uid!!)
            .setValue(user)
    }


}