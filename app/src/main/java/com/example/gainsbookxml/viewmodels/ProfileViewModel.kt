package com.example.gainsbookxml.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.database.AppDatabase
import com.example.gainsbookxml.database.entities.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).appDao

    private val _profile = MutableStateFlow(listOf<Profile>())
    val profile: StateFlow<List<Profile>> get() = _profile

    // never saved to database due to time restraints
    private val _profilePicture = MutableStateFlow("android.resource://com.example.gainsbookjc/drawable/placeholder_image")
    val profilePicture: StateFlow<String> get() = _profilePicture

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dao.getProfile()
            if (response.isEmpty()) {
                val profile = Profile(userID = 1, username = "John Doe", description = "Description goes here")
                dao.insertProfile(profile = profile)
                val response2 = dao.getProfile()
                if (response2.isNotEmpty()) {
                    _profile.emit(response2)
                }
            } else _profile.emit(response)
        }
    }

    fun setProfile(profile: Profile) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertProfile(profile)
            _profile.emit(listOf(profile))
        }
    }

    fun setProfilePicture(picture: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _profilePicture.emit(picture)
        }
    }
}

inline fun <VM : ViewModel> profileViewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }