package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val orden: Int,
    val tipo: TipoCategoria // "CANAL", "TITULO"
)

enum class TipoCategoria {
    CANAL,
    TITULO
}
