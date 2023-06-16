package com.bangkit.capstone.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bangkit.capstone.*
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ItemsPagerViewModel
import com.bangkit.capstone.databinding.ActivityHomeBinding
import com.bangkit.capstone.ui.home.camera.CameraActivity
import com.bangkit.capstone.ui.profile.ProfileFragment

@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var token = ""
    private val pref = SettingPreferences.getInstance(dataStore)
    private val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
    private val fragmentHome = HomeFragment()
    private lateinit var startNewItems: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentProfile = ProfileFragment()

        startNewItems =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    fragmentHome.onRefresh()
                }
            }

        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(this) {
                token = "Bearer $it"
                switchFragment(fragmentHome)
            }

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
               R.id.nav_home -> {
                   switchFragment(fragmentHome)
                   true
               }
               R.id.nav_scan -> {
                   if(Helper.isPermissionGranted(this, Manifest.permission.CAMERA)){
                       val intent = Intent(this@HomeActivity, CameraActivity::class.java)
                       startNewItems.launch(intent)
                       true
                   }else{
                       ActivityCompat.requestPermissions(
                           this@HomeActivity,
                           arrayOf(Manifest.permission.CAMERA),
                           Constanta.CAMERA_PERMISSION_CODE
                       )
                       false
                   }
               }
               R.id.nav_profile -> {
                   switchFragment(fragmentProfile)
                   true
               }
               else -> false
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
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun getUserToken() = token

    fun getStoryViewModel(): ItemsPagerViewModel {
        val viewModel: ItemsPagerViewModel by viewModels {
            ViewModelFactory(
                this,
                ApiConfig.getApiService(),
                getUserToken()
            )
        }
        return viewModel
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    fun routeToAuth() = startActivity(Intent(this, AuthActivity::class.java))

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}