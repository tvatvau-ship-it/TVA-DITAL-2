package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Episodio
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodioDao {
    @Query("SELECT * FROM episodios WHERE tituloId = :tituloId ORDER BY temporada ASC, numero ASC")
    fun getEpisodiosPorTitulo(tituloId: Int): Flow<List<Episodio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodio(episodio: Episodio)

    @Update
    suspend fun updateEpisodio(episodio: Episodio)

    @Query("DELETE FROM episodios WHERE id = :id")
    suspend fun deleteEpisodioById(id: Int)
}
