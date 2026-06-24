package com.example.data

import kotlinx.coroutines.flow.Flow

class CigaretteRepository(private val cigaretteDao: CigaretteDao) {
    val allRecords: Flow<List<CigaretteRecord>> = cigaretteDao.getAllRecords()

    fun getRecordForDate(date: String): Flow<CigaretteRecord?> {
        return cigaretteDao.getRecordByDate(date)
    }

    suspend fun saveRecord(date: String, count: Int) {
        val record = CigaretteRecord(date, count)
        cigaretteDao.insertRecord(record)
    }

    suspend fun deleteRecord(date: String) {
        cigaretteDao.deleteRecordByDate(date)
    }
}
