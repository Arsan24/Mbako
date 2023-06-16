package com.bangkit.capstone.ui.home.sell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.databinding.ActivitySellBinding
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bangkit.capstone.*
import com.bangkit.capstone.ui.home.HomeActivity
import java.io.File
import java.io.FileOutputStream


class SellActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySellBinding

    val sellViewModel by viewModels<SellViewModel>()
    private val imageSize = 224
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(this) { token ->
                userToken = StringBuilder("Bearer ").append(token).toString()
            }

        binding.btnPreviewImage.setOnClickListener {
            startGallery()
        }

        binding.btnAuction.setOnClickListener {
            if (
                binding.edtProductName.text.isNotEmpty() or
                binding.edtPrice.text.isNotEmpty() or
                binding.edtStok.text.isNotEmpty()
            ) {
                val drawable = binding.btnPreviewImage.drawable
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                    val file = bitmapToFile(resizedBitmap, this@SellActivity)
                    uploadImage(
                        file,
                        binding.edtProductName.text.toString(),
                        binding.edtPrice.text.toString().toInt(),
                        binding.edtStok.text.toString().toInt()
                    )
                } else {
                    Helper.showDialogInfo(
                        this,
                        getString(R.string.UI_validation_empty_data)
                    )
                }
            } else {
                Helper.showDialogInfo(
                    this,
                    getString(R.string.UI_validation_empty_data)
                )
            }
        }

        sellViewModel.let{ vm ->
            vm.isSuccessSellItems.observe(this) {
                if (it) {
                    val dialog = Helper.dialogInfoBuilder(
                        this,
                        getString(R.string.API_success_auction)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        navigateToHomeActivity()
                    }
                    dialog.show()
                }
            }
            vm.loading.observe(this) {
                binding.loading.visibility = it
            }

            vm.error.observe(this) {
                if (it.isNotEmpty()) {
                    Helper.showDialogInfo(this, it)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constanta.CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.notifyGivePermission(this, "Berikan aplikasi izin mengakses kamera  ")
                }
            }
            Constanta.LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.notifyGivePermission(
                        this,
                        "Berikan aplikasi izin lokasi untuk membaca lokasi  "
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onBackPressed() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

    private fun navigateToHomeActivity() {
        Intent(this@SellActivity, HomeActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                binding.btnPreviewImage.setImageURI(uri)

            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        // Create a file in the cache directory
        val file = File(context.cacheDir, "image.jpg")
        file.createNewFile()

        // Compress the bitmap and write it to the file
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return file
    }

    private fun uploadImage(image: File, pname: String, price: Int, quantity: Int){
        if(userToken != null){
            sellViewModel.sellItems(
                this,
                userToken!!,
                image,
                pname,
                price,
                quantity
            )
        }else{
            Helper.showDialogInfo(
                this,
                getString(R.string.API_error_header_token)
            )
        }
    }

}