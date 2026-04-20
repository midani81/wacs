package com.midani.wacs.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DetectedNumberDao {
    
    @Query("SELECT * FROM detected_numbers ORDER BY detectedAt DESC")
    fun getAll(): LiveData<List<DetectedNumber>>
    
    @Query("SELECT * FROM detected_numbers WHERE isSaved = 0 AND isIgnored = 0 ORDER BY detectedAt DESC")
    fun getPending(): LiveData<List<DetectedNumber>>
    
    @Query("SELECT * FROM detected_numbers WHERE phoneNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): DetectedNumber?
    
    @Query("SELECT COUNT(*) FROM detected_numbers WHERE isSaved = 0 AND isIgnored = 0")
    fun getPendingCount(): LiveData<Int>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(number: DetectedNumber): Long
    
    @Update
    suspend fun update(number: DetectedNumber)
    
    @Query("UPDATE detected_numbers SET isSaved = 1, contactName = :name WHERE id = :id")
    suspend fun markAsSaved(id: Long, name: String)
    
    @Query("UPDATE detected_numbers SET isIgnored = 1 WHERE id = :id")
    suspend fun markAsIgnored(id: Long)
    
    @Query("DELETE FROM detected_numbers WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM detected_numbers")
    suspend fun deleteAll()
}
