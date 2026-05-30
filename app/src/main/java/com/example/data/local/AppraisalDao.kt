package com.example.data.local

import androidx.room.*
import com.example.data.model.Appraisal
import kotlinx.coroutines.flow.Flow

@Dao
interface AppraisalDao {
    @Query("SELECT * FROM appraisals ORDER BY timestamp DESC")
    fun getAllAppraisals(): Flow<List<Appraisal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppraisal(appraisal: Appraisal)

    @Delete
    suspend fun deleteAppraisal(appraisal: Appraisal)

    @Query("DELETE FROM appraisals")
    suspend fun clearHistory()
}
