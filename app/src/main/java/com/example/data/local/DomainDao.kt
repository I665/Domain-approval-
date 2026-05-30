package com.example.data.local

import androidx.room.*
import com.example.data.model.Domain
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainDao {
    @Query("SELECT * FROM domains ORDER BY price DESC")
    fun getAllDomains(): Flow<List<Domain>>

    @Query("SELECT * FROM domains WHERE category = :category ORDER BY price DESC")
    fun getDomainsByCategory(category: String): Flow<List<Domain>>

    @Query("SELECT * FROM domains WHERE isFavorite = 1 ORDER BY price DESC")
    fun getFavoriteDomains(): Flow<List<Domain>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomain(domain: Domain)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(domains: List<Domain>)

    @Query("UPDATE domains SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteDomain(domain: Domain)

    @Query("SELECT COUNT(*) FROM domains")
    suspend fun getCount(): Int
}
