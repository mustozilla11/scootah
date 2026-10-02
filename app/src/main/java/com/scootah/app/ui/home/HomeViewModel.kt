package com.scootah.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.scootah.app.data.repository.UserRepository
import com.scootah.app.util.getGreeting
import kotlinx.coroutines.flow.map

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository.getInstance(application)

    val userProfile = repository.userProfile

    val greeting = userProfile.map { profile ->
        if (profile.userName.isNotBlank()) getGreeting(profile.userName)
        else getGreeting("Sürücü")
    }
}
