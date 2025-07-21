package com.example.khedutg

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.khedutg.databinding.FragmentProfileBinding
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import com.example.khedutg.viewmodels.UserViewModel
import androidx.core.content.ContextCompat
import com.example.khedutg.activity.AuthMainActivity
import com.example.khedutg.databinding.AddressBookLayoutBinding



class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel : UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        onBackButtonClicked()
        onAddressBookClicked()
        onOrderLayoutClicked()
        onLogOutClicked()
        setUserPhoneNumber()
        setStatusBarColor()
        return binding.root
    }

    private fun setUserPhoneNumber() {
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        firebaseUser?.phoneNumber?.let { phone ->
            binding.tvUserPhone.text = phone
        }
    }

    private fun onOrderLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }

    private fun onLogOutClicked() {
        binding.llLogout.setOnClickListener {
            val bundle = AlertDialog.Builder(requireContext())
            val alertDialog = bundle.create()
            bundle.setTitle("Log Out")
                .setMessage("Do You Want To Log Out")
                .setPositiveButton("Yes"){_,_ ->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }
    }

    private fun onAddressBookClicked() {
        binding.llAddress.setOnClickListener {
            val addressBookLayoutBinding = AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress { address->
                addressBookLayoutBinding.etAddress.setText(address.toString())
            }
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialog.show()
            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled = true
            }
            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext(), "Address Updated")
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.fullgreen)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
