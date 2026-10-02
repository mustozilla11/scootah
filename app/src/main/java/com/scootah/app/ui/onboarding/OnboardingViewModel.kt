package com.scootah.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scootah.app.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val userName: String = "",
    val scooterName: String = "",
    val userNameError: String? = null,
    val scooterNameError: String? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository.getInstance(application)

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onUserNameChange(value: String) {
        _uiState.update { it.copy(userName = value, userNameError = null) }
    }

    fun onScooterNameChange(value: String) {
        _uiState.update { it.copy(scooterName = value, scooterNameError = null) }
    }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.userName.isBlank()) {
            _uiState.update { it.copy(userNameError = "Ad boş bırakılamaz") }
            hasError = true
        }
        if (state.scooterName.isBlank()) {
            _uiState.update { it.copy(scooterNameError = "Scooter ismi boş bırakılamaz") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // Persist to DataStore — survives app restart
            repository.saveProfile(
                userName = state.userName.trim(),
                scooterName = state.scooterName.trim()
            )
            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }
}
