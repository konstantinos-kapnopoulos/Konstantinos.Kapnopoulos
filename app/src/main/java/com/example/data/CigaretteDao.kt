package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CigaretteDao {
    @Query("SELECT * FROM cigarette_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CigaretteRecord>>

    @Query("SELECT * FROM cigarette_records WHERE date = :date LIMIT 1")
    fun getRecordByDate(date: String): Flow<CigaretteRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CigaretteRecord)

    @Query("DELETE FROM cigarette_records WHERE date = :date")
    suspend fun deleteRecordByDate(date: String)
}
