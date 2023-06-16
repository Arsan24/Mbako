package com.bangkit.capstone.ui.register

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.fragment.app.activityViewModels
import com.bangkit.capstone.AuthActivity
import com.bangkit.capstone.AuthViewModel
import com.bangkit.capstone.Helper
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.FragmentRegisterBinding
import com.bangkit.capstone.ui.login.LoginFragment

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registerResult.observe(viewLifecycleOwner) { register ->
            if (!register.error) {
                val dialog = Helper.dialogInfoBuilder(
                    (activity as AuthActivity),
                    getString(R.string.UI_info_successful_register_user)
                )
                val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                btnOk.setOnClickListener {
                    dialog.dismiss()
                    switchLogin()
                    requireActivity().finish()
                }
                dialog.show()
            }
        }

        // Pemantau LiveData error
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                val errorMessage = getString(R.string.UI_validation_username_exists)
                if (error.contains(errorMessage)) {
                    Helper.showDialogInfo(requireContext(), errorMessage)
                } else {
                    Helper.showDialogInfo(requireContext(), error)
                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner){state ->
            binding.loading.visibility = state
        }

        binding.signupButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val contact = binding.contactEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            when {
                username.isEmpty() or contact.isEmpty() or email.isEmpty() or password.isEmpty() -> {
                    Helper.showDialogInfo(
                        requireContext(),
                        getString(R.string.UI_validation_empty_username_contact_email_password)
                    )
                }
                else -> {
                    viewModel.register(username, contact, email, password)
                }
            }

            viewModel.error.observe(viewLifecycleOwner) { error ->
                if (error == "Email is already taken") {
                    Helper.showDialogInfo(requireContext(), getString(R.string.UI_validation_email_exists))
                }
            }
        }

        binding.tvLogin.setOnClickListener{
            switchLogin()
        }

        setupView()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun switchLogin() {
        viewModel.error.postValue("")
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.container, LoginFragment(), LoginFragment::class.java.simpleName)
            commit()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}