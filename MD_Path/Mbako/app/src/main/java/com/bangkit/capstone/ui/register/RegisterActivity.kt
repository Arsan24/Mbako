package com.bangkit.capstone.ui.register

import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityRegisterBinding
import com.bangkit.capstone.ui.home.HomeActivity
import com.bangkit.capstone.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        auth = FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if(email.isEmpty()){
                binding.emailEditText.error = "Email harus diisi"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        binding.tvLogin.setOnClickListener{
            Intent(this@RegisterActivity, LoginActivity::class.java).also{
                startActivity(it)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    showCustomDialog()
                }else{
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_info)

        val buttonOk = dialog.findViewById<Button>(R.id.button_ok)
        val textViewMessage = dialog.findViewById<TextView>(R.id.message)

        textViewMessage.text = getString(R.string.UI_info_successful_register_user)
        buttonOk.setOnClickListener {
            dialog.dismiss()
            navigateToHomeActivity()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_loading_container)

        dialog.show()
    }

    private fun navigateToHomeActivity() {
        Intent(this@RegisterActivity, HomeActivity::class.java).also {
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