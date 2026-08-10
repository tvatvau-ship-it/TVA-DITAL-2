package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Canal
import kotlinx.coroutines.flow.Flow

@Dao
interface CanalDao {
    @Query("SELECT * FROM canales WHERE oculto = 0 ORDER BY orden ASC")
    fun getAllCanales(): Flow<List<Canal>>

    @Query("SELECT * FROM canales WHERE categoriaId = :categoriaId AND oculto = 0 ORDER BY orden ASC")
    fun getCanalesPorCategoria(categoriaId: Int): Flow<List<Canal>>

    @Query("SELECT * FROM canales WHERE esFavorito = 1 AND oculto = 0 ORDER BY orden ASC")
    fun getCanalesFavoritos(): Flow<List<Canal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanal(canal: Canal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanales(canales: List<Canal>)

    @Transaction
    suspend fun insertCanalesInTransaction(canales: List<Canal>) {
        canales.chunked(100).forEach { insertCanales(it) }
    }

    @Update
    suspend fun updateCanal(canal: Canal)

    @Query("DELETE FROM canales WHERE id = :id")
    suspend fun deleteCanalById(id: Int)

    @Query("DELETE FROM canales")
    suspend fun deleteAllCanales()
}
