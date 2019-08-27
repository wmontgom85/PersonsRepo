package com.wmontgom.personsrepo.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Person(
    @PrimaryKey(autoGenerate = true) @NonNull val _id: Long,
    val gender: String?,
    @Ignore val name: Name?,
    @Ignore val location: Location?,
    val email: String?,
    @Ignore val dob: DOB?,
    val phone: String?,
    val cell: String?,
    @Ignore val picture: Picture?,
    var firstName : String? = "",
    var lastName : String? = "",
    var street : String? = "",
    var city : String? = "",
    var state : String? = "",
    var postcode : String? = "",
    var birthdate : String? = "",
    var avatarLarge : String? = "",
    var avatarMedium : String? = "",
    var thumbnail : String? = ""
) {
    fun fill() {
        name?.let {
            firstName = it.first
            lastName = it.last
        }

        location?.let {
            street = it.street
            city = it.city
            state = it.state
            postcode = it.postcode
        }

        dob?.let {
            birthdate = it.date
        }

        picture?.let {
            avatarLarge = it.large
            avatarMedium = it.medium
            thumbnail = it.thumbnail
        }
    }
}

data class Name(
    val first: String?,
    val last: String?
)

data class Location(
    val street: String?,
    val city: String?,
    val state: String?,
    val postcode: String
)

data class DOB(
    val date: String?
)

data class Picture(
    val large: String?,
    val medium: String?,
    val thumbnail: String?
)