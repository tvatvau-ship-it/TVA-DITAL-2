package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "titulos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Titulo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val poster: String,
    val sinopsis: String,
    val anio: Int,
    val categoriaId: Int?,
    val tipoContenido: TipoContenido,
    val fuenteLicencia: String,
    val esFavorito: Boolean = false,
    val oculto: Boolean = false,
    val orden: Int,
    val streamUrl: String? = null, // Used if it's a movie
    val duracionVista: Long = 0L   // Used if it's a movie
)

enum class TipoContenido {
    PELICULA,
    SERIE
}
