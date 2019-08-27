package com.wmontgom.personsrepo.api

import com.wmontgom.personsrepo.model.Person

data class PersonsApiResponse(
    val results : List<Person>?
)