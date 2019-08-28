package com.wmontgom.personsrepo.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Person(
    @PrimaryKey(autoGenerate = true) var _id: Long,
    var gender: String?,
    @Ignore var name: Name?,
    @Ignore var location: Location?,
    var email: String?,
    @Ignore var dob: DOB?,
    var phone: String?,
    var cell: String?,
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
    constructor() : this(0L,"",Name("",""),Location("","","",""),"",DOB(""),"","",Picture("","",""),"","","","","","","","","","")

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

    fun buildAddress() : String {
        var line1 = ""
        var line2 = ""

        street?.let { s -> line1 = s.split(' ').joinToString(" ") { it.capitalize() } }

        city?.let {
            line2 = it.capitalize()
        }

        state?.let {
            line2 += when {
                line2.isNotEmpty() -> ", ${it.capitalize()}"
                else -> it.capitalize()
            }
        }

        postcode?.let {
            line2 += when {
                line2.isNotEmpty() -> ", $it"
                else -> it
            }
        }

        return when {
            (line1.isNotEmpty() && line2.isNotEmpty()) -> "$line1 \n$line2"
            line1.isNotEmpty() -> line1
            line2.isNotEmpty() -> line2
            else -> ""
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