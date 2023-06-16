package com.bangkit.capstone.ui.forgotpassword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bangkit.capstone.Helper
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityForgotPasswordBinding
import com.bangkit.capstone.ui.resetpassword.ResetPasswordActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel by viewModels<ForgotPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.let{ vm ->
            vm.forgotPassResult.observe(this){ forgotPass ->
                if(!forgotPass.error){
                    val dialog = Helper.dialogInfoBuilder(
                        this,
                        getString(R.string.UI_info_successful_getToken)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        switchToReset()
                    }
                    dialog.show()
                }
            }
            vm.error.observe(this){ error ->
                if(error.isNotEmpty()){
                    val errorMessage = getString(R.string.UI_validation_email_noExist)
                    if (error.contains(errorMessage)) {
                        Helper.showDialogInfo(this, errorMessage)
                    } else {
                        Helper.showDialogInfo(this, error)
                    }
                }
            }
            vm.loading.observe(this){ state ->
                binding.loading.visibility = state
            }
        }

        binding.btnSend.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            when{
                email.isEmpty() -> {
                    Helper.showDialogInfo(
                        this,
                        getString(R.string.UI_validation_empty_email)
                    )
                }
                else -> {
                    viewModel.forgotPassword(email)
                }

            }
        }
    }

    private fun switchToReset() {
        Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java).apply {
            startActivity(this)
        }
    }

}