package com.wmontgom.personsrepo.dao

import androidx.room.*
import com.wmontgom.personsrepo.model.Person

@Dao
public interface PersonDao {
    @Query("SELECT * FROM Person")
    fun getPeople(): List<Person>

    @Query("SELECT * FROM Person WHERE _id = :id")
    fun getPerson(id: Long) : Person

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(person: Person)

    @Delete
    fun delete(person: Person)

    @Query("DELETE FROM Person")
    fun deleteAll()
}