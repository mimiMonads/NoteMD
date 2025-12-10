package com.example.notemd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notemd.NoteMDApplication
import com.example.notemd.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkThemeEnabled: Boolean = false
)

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val uiState = repository.darkThemeEnabled
        .map { enabled -> SettingsUiState(darkThemeEnabled = enabled) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(enabled)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteMDApplication)
                SettingsViewModel(application.container.settingsRepository)
            }
        }
    }
}
