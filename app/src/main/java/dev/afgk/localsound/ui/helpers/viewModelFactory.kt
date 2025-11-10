package dev.afgk.localsound.ui.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <VW : ViewModel> viewModelFactory(initializer: () -> VW): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}