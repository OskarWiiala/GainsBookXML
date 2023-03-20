package com.example.gainsbookxml.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
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

class ProfileFragment : Fragment() {
    val TAG = "ProfileFragment"
    private lateinit var binding: FragmentProfileBinding
    private lateinit var singlePhotoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        singlePhotoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
            ActivityResultCallback<Uri?> {
                // Add same code that you want to add in onActivityResult method
                profileViewModel.setProfilePictureTemp(it.toString())
            })

    }

    // Used to handle changing the month and year
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
        lifecycleScope.launch {
            profileViewModel.profilePicture.collect {
                val newUri = Uri.parse(it)
                Log.d("TAG", "in collect pic: $it")
                binding.uri = newUri
                /*binding.profilePicture.setImageURI(Uri.parse(it))*/
            }
        }

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