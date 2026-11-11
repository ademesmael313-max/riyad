package com.riyadsalihin.amharic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Lets each screen build its ViewModel with plain constructor injection. */
class AppViewModelFactory(private val create: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}
