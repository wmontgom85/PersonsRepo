package com.wmontgom.personsrepo.viewmodel

import androidx.lifecycle.ViewModel
import com.wmontgom.personsrepo.api.APIHandler

abstract class BaseViewModel: ViewModel() {
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(APIHandler)
        .build()

    init {
        inject()
    }

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is PersonsViewModel -> injector.injectFormsVM(this)
        }
    }
}