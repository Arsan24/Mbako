package com.bangkit.capstone.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.FragmentChangePasswordBinding
import com.bangkit.capstone.ui.updateemail.UpdateEmailFragmentDirections
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        binding.layoutPassword.visibility = View.VISIBLE
        binding.layoutNewPassword.visibility = View.GONE

        binding.btnAuth.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()

            if(password.isEmpty()){
                binding.etPassword.error = "Password harus diisi"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            user?.let {
                val userCredential = EmailAuthProvider.getCredential(it.email!!, password)
                it.reauthenticate(userCredential).addOnCompleteListener{
                    if(it.isSuccessful){
                        binding.layoutPassword.visibility = View.GONE
                        binding.layoutNewPassword.visibility = View.VISIBLE
                    }else if(it.exception is FirebaseAuthInvalidCredentialsException){
                        binding.etPassword.error = "Password salah"
                        binding.etPassword.requestFocus()
                    }else{ //error gak ada koneksi
                        Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.btnUpdate.setOnClickListener { view ->
                val newPassword = binding.etNewPassword.text.toString().trim()
                val newPasswordConfirm = binding.etNewPasswordConfirm.text.toString().trim()

                if(newPassword != newPasswordConfirm){
                    binding.etNewPasswordConfirm.error = "Password tidak sama"
                    binding.etNewPasswordConfirm.requestFocus()
                    return@setOnClickListener
                }

                user?.let{
                    user.updatePassword(newPassword).addOnCompleteListener{
                        if(it.isSuccessful){
                            val actionPasswordChanged = ChangePasswordFragmentDirections.actionPasswordChange()
                            Navigation.findNavController(view).navigate(actionPasswordChanged)
                            Toast.makeText(activity, "Password berhasil diganti",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(activity,"${it.exception?.message}" , Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}