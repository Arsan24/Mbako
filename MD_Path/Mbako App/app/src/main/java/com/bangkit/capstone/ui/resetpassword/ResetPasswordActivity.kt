package com.bangkit.capstone.ui.resetpassword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import com.bangkit.capstone.AuthActivity
import com.bangkit.capstone.Helper
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityForgotPasswordBinding
import com.bangkit.capstone.databinding.ActivityResetPasswordBinding
import com.bangkit.capstone.ui.forgotpassword.ForgotPasswordViewModel
import com.bangkit.capstone.ui.login.LoginFragment

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel by viewModels<ResetPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.let{ vm ->
            vm.resetResult.observe(this){ forgotPass ->
                if(!forgotPass.error){
                    val dialog = Helper.dialogInfoBuilder(
                        this,
                        getString(R.string.UI_info_successful_reset_password)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        switchToLogin()
                    }
                    dialog.show()
                }
            }
            vm.error.observe(this){ error ->
                error?.let {
                    if(it.isNotEmpty()){
                        Helper.showDialogInfo(this, it)
                    }
                }
            }
            vm.loading.observe(this){ state ->
                binding.loading.visibility = state
            }
        }

        binding.btnReset.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val token = binding.tokenEditText.text.toString()
            val new_password = binding.passwordEditText.text.toString()
            when {
                username.isEmpty() or token.isEmpty() or new_password.isEmpty() ->{
                    Helper.showDialogInfo(
                        this,
                        getString(R.string.UI_validation_empty_username_token_password)
                    )
                }
                else -> {
                    viewModel.resetPassword(username, token, new_password)
                }
            }
        }

    }

    private fun switchToLogin() {
        Intent(this@ResetPasswordActivity, AuthActivity::class.java).apply {
            startActivity(this)
        }
    }
}