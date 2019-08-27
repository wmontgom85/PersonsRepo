package com.wmontgom.personsrepo.dao

import androidx.room.*
import com.wmontgom.personsrepo.model.Person

public interface PersonDao {
    @Query("SELECT * FROM Person")
    fun getPeople(): List<Person>

    @Query("SELECT * FROM Person WHERE id = :id")
    fun getAmount(id: Long) : Person

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(person: Person)

    @Delete
    fun delete(person: Person)

    @Query("DELETE FROM Person")
    fun deleteAll()
}