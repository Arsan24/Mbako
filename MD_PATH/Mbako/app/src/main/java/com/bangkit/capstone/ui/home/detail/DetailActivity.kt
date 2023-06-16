package com.bangkit.capstone.ui.home.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import com.bangkit.capstone.*
import com.bangkit.capstone.databinding.ActivityDetailBinding
import com.bangkit.capstone.ml.ModelV4
import com.bangkit.capstone.ui.home.HomeActivity
import com.bangkit.capstone.ui.resetpassword.ResetPasswordViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.NumberFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel by viewModels<DetailViewModel>()

    var num = 0
    private val imageSize = 224
    private var initialPrice = 0
    private var token: String = ""

    private fun getToken() {
        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(this) { userToken ->
                token = "Bearer $userToken"
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val maxStok = intent.getIntExtra(Constanta.itemsDetail.UserQuantity.name, 0)
        num = 1
        binding.tvStok.text = num.toString()

        initialPrice = intent.getIntExtra(Constanta.itemsDetail.UserPrice.name, 0)

        binding.btnPlus.setOnClickListener {
            if (num < maxStok) {
                num++
                binding.tvStok.text = num.toString()
                updatePrice()
            }

            if (num >= maxStok) {
                binding.btnPlus.isEnabled = false
            }
            binding.btnMinus.isEnabled = true
        }

        binding.btnMinus.setOnClickListener {
            num--
            binding.tvStok.text = num.toString()
            checkMinusButtonStatus()
            updatePrice()
            if (num <= 0) {
                binding.btnMinus.isEnabled = false
            }
            binding.btnPlus.isEnabled = true
        }

        Glide.with(binding.root)
            .load(intent.getData(Constanta.itemsDetail.ImageUrl.name, ""))
            .into(binding.btnPreviewImage)
        binding.tvProductName.text = intent.getData(Constanta.itemsDetail.UserProductName.name, "")
        binding.itemUploadTime.text = intent.getData(Constanta.itemsDetail.UploadTime.name, "Upload time")
        val price = intent.getIntExtra(Constanta.itemsDetail.UserPrice.name, 0)
        val formattedPrice = "Rp. ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)}"
        binding.tvPrice.text = formattedPrice

        binding.btnScan.setOnClickListener {
            val drawable = binding.btnPreviewImage.drawable
            val bitmap = (drawable as BitmapDrawable).bitmap
            binding.progressBar.visibility = View.VISIBLE
            binding.btnScan.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val classificationResult = withContext(Dispatchers.Default) {
                    classifyImage(bitmap)
                }
                binding.tvHasilScan.text = classificationResult
                binding.progressBar.visibility = View.GONE
                binding.btnScan.isEnabled = true
            }
        }

        viewModel.let { vm ->
            vm.sellResult.observe(this){sellItem ->
                if(!sellItem.error){
                    val dialog = Helper.dialogInfoBuilder(
                        this,
                        getString(R.string.UI_info_successful_buy)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        switchToHome()
                    }
                    dialog.show()
                }
            }
            vm.error.observe(this){error ->
                error?.let{
                    if(it.isNotEmpty()){
                        Helper.showDialogInfo(this, it)
                    }
                }
            }
            vm.loading.observe(this){ state ->
                binding.progressBar.visibility = state
            }
        }

        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(this) { userToken ->
                token = "Bearer $userToken"
            }
        getToken()
        binding.btnBuy.setOnClickListener {
            val itemId = intent.getStringExtra(Constanta.itemsDetail.ItemId.name) ?: ""
            val quantity = num

            val priceText = binding.tvPrice.text.toString()
            val priceValue = priceText
                .replace("Rp.", "") // Menghapus simbol mata uang
                .replace(".", "") // Menghapus pemisah ribuan
                .trim() // Menghapus spasi yang mungkin ada

            val price = try {
                priceValue.toInt()
            } catch (e: NumberFormatException) {
                0 // Jika konversi gagal, gunakan nilai default 0
            }

            if (price < 1) {
                Helper.showDialogInfo(
                    this,
                    getString(R.string.UI_validation_stock)
                )
            } else {
                viewModel.sellItems(token, itemId, quantity)
            }
        }

    }
    private fun Intent.getData(key: String, defaultValue: String = "None"): String {
        return getStringExtra(key) ?: defaultValue
    }

    private fun classifyImage(image: Bitmap): String {
        var resultText = ""

        try {
            val model = ModelV4.newInstance(applicationContext)

            val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(imageSize * imageSize)
            resizedImage.getPixels(intValues, 0, resizedImage.width, 0, 0, resizedImage.width, resizedImage.height)
            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16) and 0xFF) * (1.0f / 255))
                    byteBuffer.putFloat(((value shr 8) and 0xFF) * (1.0f / 255))
                    byteBuffer.putFloat((value and 0xFF) * (1.0f / 255))
                }
            }

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.getOutputFeature0AsTensorBuffer()

            val confidences = outputFeature0.floatArray
            var maxPos = 0
            var maxConfidence = 0.0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf("Rendah", "Sedang", "Tinggi", "Tembakau Tidak Terdeteksi")
            resultText = classes[maxPos]

            model.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return resultText
    }

    private fun switchToHome() {
        Intent(this@DetailActivity, HomeActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun updatePrice() {
        val updatedPrice = initialPrice * num
        val formattedPrice = "Rp. ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(updatedPrice)}"
        binding.tvPrice.text = formattedPrice
    }
    private fun checkMinusButtonStatus() {
        binding.btnMinus.isEnabled = num != 0
    }
}