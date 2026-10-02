package com.scootah.app.ui.garage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.scootah.app.data.repository.UserRepository

class GarageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository.getInstance(application)
    val userProfile = repository.userProfile
}
