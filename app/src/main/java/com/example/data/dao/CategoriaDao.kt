package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Categoria
import com.example.data.model.TipoCategoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias WHERE tipo = :tipo ORDER BY orden ASC")
    fun getCategoriasPorTipo(tipo: TipoCategoria): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE tipo = :tipo")
    suspend fun getSyncCategorias(tipo: TipoCategoria): List<Categoria>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: Categoria)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorias(categorias: List<Categoria>)

    @Update
    suspend fun updateCategoria(categoria: Categoria)

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun deleteCategoriaById(id: Int)

    @Query("DELETE FROM categorias WHERE tipo = :tipo")
    suspend fun deleteCategoriasPorTipo(tipo: TipoCategoria)
}
