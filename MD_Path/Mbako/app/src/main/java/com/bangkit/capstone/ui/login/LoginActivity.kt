package com.bangkit.capstone.ui.login

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityLoginBinding
import com.bangkit.capstone.ui.home.HomeActivity
import com.bangkit.capstone.ui.register.RegisterActivity
import com.bangkit.capstone.ui.resetpassword.ResetPasswordActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if(email.isEmpty()){
                binding.emailEditText.error = "Email harus diisi"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }

            loginUser(email,password)
        }

        binding.tvRegister.setOnClickListener{
            Intent(this@LoginActivity, RegisterActivity::class.java).also{
                startActivity(it)
            }
        }

        binding.btnForgotPassword.setOnClickListener{
            Intent(this@LoginActivity, ResetPasswordActivity::class.java).also{
                startActivity(it)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    navigateToHomeActivity()
                }else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHomeActivity() {
        Intent(this@LoginActivity, HomeActivity::class.java).also{
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null){
            navigateToHomeActivity()
        }
    }
}