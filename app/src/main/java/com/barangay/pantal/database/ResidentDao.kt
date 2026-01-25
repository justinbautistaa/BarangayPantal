package com.barangay.pantal.database

import androidx.room.*
import com.barangay.pantal.models.Resident
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
    @Insert
    suspend fun insert(resident: Resident)

    @Query("SELECT * FROM residents ORDER BY firstName ASC")
    fun getAllResidents(): Flow<List<Resident>>

    @Query("SELECT COUNT(*) FROM residents")
    suspend fun getCount(): Int
}