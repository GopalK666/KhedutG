package com.example.khedutg.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.khedutg.R
import com.example.khedutg.Utils
import com.example.khedutg.activity.UsersMainActivity
import com.example.khedutg.databinding.FragmentOTPBinding
import com.example.khedutg.models.Users
import com.example.khedutg.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentOTPBinding? = null
    private val binding get() = _binding!!
    private lateinit var userNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPBinding.inflate(inflater, container, false)

        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClick()
        onBackButtonClick()
        setStatusBarColor()

        return binding.root
    }

    private fun setStatusBarColor() {
            activity?.window?.apply {
                val statusBarColorRes = ContextCompat.getColor(requireContext(), R.color.greenbox)
                statusBarColor = statusBarColorRes
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }

    private fun onLoginButtonClick() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing you...")
            val editTexts = arrayOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4, binding.etOtp5, binding.etOtp6)
            val otp = editTexts.joinToString("") { it.text.toString() }

            if (otp.length < editTexts.size){
                Utils.showToast(requireContext(), "Please enter right OTP")
            }
            else{
                editTexts.forEach { it.text?.clear() ; it.clearFocus() }
                verifyOTP(otp)
            }
        }
    }

    private fun verifyOTP(otp: String) {
        viewModel.signInWithPhoneAuthCredential(otp, userNumber, requireContext())


        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect { isSignedIn ->
                if (isSignedIn) {
                    val uid = Utils.getCurrentUserUid()
                    if (uid != null) {
                        val user = Users(uid = uid, userPhoneNumber = userNumber, userAddress = " ")
                        viewModel.saveUserToFirestore(user)
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Logged In...")
                        startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Sign in failed. Please try again.")
                    }
                }
            }
        }
    }


    //OTP function
    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP...")
        viewModel.apply {
            sendOTP(userNumber, requireActivity())
            lifecycleScope.launchWhenStarted {
                viewModel.otpSent.collect { isSent ->
                    if (isSent) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "OTP sent successfully")
                    }
                }
            }
        }
    }

    private fun onBackButtonClick() {
        binding.tbOtpfragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }
    //OTP user enter kare te Function
    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4, binding.etOtp5, binding.etOtp6)
        for (i in editTexts.indices){
            editTexts[i].addTextChangedListener (object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1){
                        if (i < editTexts.size - 1){
                            editTexts[i + 1].requestFocus()
                        }
                    }
                    else if (s?.length == 0){
                        if (i > 0){
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }

            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("phoneNumber").toString()

        binding.tvUserNumber.text = userNumber
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}