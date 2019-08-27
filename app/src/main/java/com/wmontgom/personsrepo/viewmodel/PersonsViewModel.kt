package com.wmontgom.personsrepo.viewmodel

import androidx.lifecycle.MutableLiveData
import com.wmontgom.personsrepo.api.APIHandler.safeApiCall
import com.wmontgom.personsrepo.api.PersonsApi
import com.wmontgom.personsrepo.model.Person
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PersonsViewModel : BaseViewModel() {
    @Inject
    lateinit var personsApi: PersonsApi

    //create a new Job
    private val parentJob = Job()

    //create a coroutine context with the job and the dispatcher
    private val coroutineContext : CoroutineContext get() = parentJob + Dispatchers.Default

    //create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)

    //live data that will be populated as forms update
    val formsLiveData = MutableLiveData<Person>()

    fun getLatestForms() {
        ///launch the coroutine scope
        scope.launch {
            //get latest forms from forms repo
            val person = safeApiCall(
                //await the result of deferred type
                call = { personsApi.getRandomPerson() },
                errorMessage = "Error fetching forms"
                //convert to mutable list
            )?.let {
                //post the value inside live data
                formsLiveData.postValue(it)
            }
        }
    }

    fun cancelRequests() = coroutineContext.cancel()
}