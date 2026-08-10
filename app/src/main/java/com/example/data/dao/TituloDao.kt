package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Titulo
import kotlinx.coroutines.flow.Flow

@Dao
interface TituloDao {
    @Query("SELECT * FROM titulos WHERE oculto = 0 ORDER BY orden ASC")
    fun getAllTitulos(): Flow<List<Titulo>>

    @Query("SELECT * FROM titulos WHERE categoriaId = :categoriaId AND oculto = 0 ORDER BY orden ASC")
    fun getTitulosPorCategoria(categoriaId: Int): Flow<List<Titulo>>

    @Query("SELECT * FROM titulos WHERE id = :id")
    suspend fun getTituloById(id: Int): Titulo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitulo(titulo: Titulo)

    @Update
    suspend fun updateTitulo(titulo: Titulo)

    @Query("DELETE FROM titulos WHERE id = :id")
    suspend fun deleteTituloById(id: Int)
}
