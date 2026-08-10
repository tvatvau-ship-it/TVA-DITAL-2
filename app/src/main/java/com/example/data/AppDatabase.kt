package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.CanalDao
import com.example.data.dao.CategoriaDao
import com.example.data.dao.EpisodioDao
import com.example.data.dao.TituloDao
import com.example.data.model.Canal
import com.example.data.model.Categoria
import com.example.data.model.Episodio
import com.example.data.model.Titulo

@Database(
    entities = [Canal::class, Categoria::class, Titulo::class, Episodio::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun canalDao(): CanalDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun tituloDao(): TituloDao
    abstract fun episodioDao(): EpisodioDao
}
