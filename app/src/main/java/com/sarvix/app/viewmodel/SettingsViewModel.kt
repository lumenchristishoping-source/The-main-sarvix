package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
) : ViewModel() {
    // Settings state managed by individual screens
    // This ViewModel can be expanded for settings-specific logic
}
