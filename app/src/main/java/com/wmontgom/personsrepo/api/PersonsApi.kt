package com.wmontgom.personsrepo.api

import com.wmontgom.personsrepo.model.Person
import retrofit2.Response
import retrofit2.http.GET

interface PersonsApi {
    // attempts get a random person
    @GET("?results=1")
    suspend fun getRandomPerson(): Response<PersonsApiResponse>
}