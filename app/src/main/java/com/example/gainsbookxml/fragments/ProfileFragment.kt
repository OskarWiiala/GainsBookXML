package com.example.gainsbookxml.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gainsbookxml.databinding.FragmentProfileBinding
import com.example.gainsbookxml.utils.editProfilePopup
import com.example.gainsbookxml.viewmodels.ProfileViewModel
import com.example.gainsbookxml.viewmodels.ProfileViewModelFactory
import kotlinx.coroutines.launch

/**
 * This fragment is used to display and edit user profile information, such as username, user description and photo
 * @author Oskar Wiiala
 */
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var singlePhotoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launcher for picking a single photo with result callback
        singlePhotoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            // Do this when photo is picked
            profileViewModel.setProfilePictureTemp(it.toString())
        }
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        initUI()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        // Update profile photo whenever it is updated in the view model
        lifecycleScope.launch {
            profileViewModel.profilePicture.collect {
                val newUri = Uri.parse(it)
                binding.uri = newUri
            }
        }

        // Update username and user description whenever it is updated in the view model
        lifecycleScope.launch {
            profileViewModel.profile.collect {
                val newUserName = it.firstOrNull()?.username ?: "null"
                val newUserDescription = it.firstOrNull()?.description ?: "null"

                binding.name = newUserName
                binding.description = newUserDescription
            }
        }

        binding.buttonEdit.setOnClickListener {
            editProfilePopup(
                profileViewModel = profileViewModel,
                context = requireContext(),
                singlePhotoPickerLauncher = singlePhotoPickerLauncher,
                lifecycleScope = lifecycleScope
            )
        }
    }
}