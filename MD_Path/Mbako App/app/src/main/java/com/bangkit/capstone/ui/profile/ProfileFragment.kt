package com.bangkit.capstone.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bangkit.capstone.*
import com.bangkit.capstone.databinding.FragmentProfileBinding
import com.bangkit.capstone.ui.home.HomeActivity


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = SettingPreferences.getInstance((requireContext()).dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserName.name)
            .observe(viewLifecycleOwner){
                binding.textUsername.text = it
            }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserContact.name)
            .observe(viewLifecycleOwner){
                binding.textContact.text = it
            }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserEmail.name)
            .observe(viewLifecycleOwner){
                binding.textEmail.text = it
            }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserLastLogin.name)
            .observe(viewLifecycleOwner){
                binding.textLastLogin.text=
                    StringBuilder(getString(R.string.login_on))
                        .append(" ")
                        .append(Helper.getSimpleDateString(it))
            }
        settingViewModel.getUserPreferences(Constanta.UserPreferences.UserToken.name)
            .observe(viewLifecycleOwner){
                if(it == Constanta.preferenceDefaultValue){
                    (activity as HomeActivity).routeToAuth()
                }
            }

        binding.btnLogout.setOnClickListener {
            settingViewModel.clearUserPreferences()
        }
        binding.btnSetPermission.setOnClickListener{
            Helper.openSettingPermission(requireContext())
        }
        binding.btnSetLanguage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}