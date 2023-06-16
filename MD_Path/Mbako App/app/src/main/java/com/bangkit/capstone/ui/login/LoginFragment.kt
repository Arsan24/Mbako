package com.bangkit.capstone.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bangkit.capstone.*
import com.bangkit.capstone.databinding.FragmentLoginBinding
import com.bangkit.capstone.ui.forgotpassword.ForgotPasswordActivity
import com.bangkit.capstone.ui.register.RegisterFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = SettingPreferences.getInstance((activity as AuthActivity).dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        viewModel.let { vm ->
            vm.loginResult.observe(viewLifecycleOwner) { login ->
                val contact = login.loginResult.contact ?: ""
                settingViewModel.setUserPreferences(
                    login.loginResult.token,
                    viewModel.tempUsername.value ?: Constanta.preferenceDefaultValue,
                    contact,
                    login.loginResult.email
                )
            }
            vm.error.observe(viewLifecycleOwner){ error ->
                error?.let{
                    if(it.isNotEmpty()){
                        Helper.showDialogInfo(requireContext(), it)
                    }
                }
            }
            vm.loading.observe(viewLifecycleOwner){state ->
                binding.loading.visibility = state
            }
        }

        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(viewLifecycleOwner){ token ->
                if ( token != Constanta.preferenceDefaultValue) (activity as AuthActivity).routeToHomeActivity()
            }

        binding.loginButton.setOnClickListener {
            val username = binding.userNamelEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            when {
                username.isEmpty() or password.isEmpty() -> {
                    Helper.showDialogInfo(
                        requireContext(),
                        getString(R.string.UI_validation_empty_username_password)
                    )
                }
                else -> {
                    viewModel.login(username, password)
                }
            }
        }

        binding.tvRegister.setOnClickListener{
            viewModel.error.postValue("")
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.container, RegisterFragment(), RegisterFragment::class.java.simpleName)
                commit()
            }
        }

        binding.btnForgotPassword.setOnClickListener{
            Intent(requireContext(), ForgotPasswordActivity::class.java).also{
                startActivity(it)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}