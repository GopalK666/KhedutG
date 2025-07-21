package com.example.khedutg.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.khedutg.R
import com.example.khedutg.Utils
import com.example.khedutg.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    //Continue Button Click Function
    private fun onContinueButtonClick() {
        binding.btnLetsGo.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString()

            if (phoneNumber.isEmpty() || phoneNumber.length != 10) {
                Utils.showToast(requireContext(), "Please enter a valid phone number")
            }
            else{
                val bundle = Bundle()
                bundle.putString("phoneNumber", phoneNumber)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment, bundle)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifImageView: ImageView = binding.imgGif

        Glide.with(this)
            .asGif()
            .load(R.raw.khedut)
            .into(gifImageView)

        setStatusBarColor()

        getUserNumber()

        onContinueButtonClick()

    }
    //User Phone Number Validation
    private fun getUserNumber() {
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0

                if (len == 10) {
                    binding.btnLetsGo.isEnabled = true
                    binding.btnLetsGo.setBackgroundResource(R.drawable.button_shape)
                    binding.btnLetsGo.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.fullgreen)
                } else {
                    binding.btnLetsGo.isEnabled = false
                    binding.btnLetsGo.setBackgroundResource(R.drawable.button_shape)
                    binding.btnLetsGo.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.graylishblue)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    //Aa Se Mobile StatusBar Color Change Karvani
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.green)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
