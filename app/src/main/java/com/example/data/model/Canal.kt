package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "canales",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Canal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val logoUrl: String,
    val categoriaId: Int?,
    val streamUrl: String,
    val userAgent: String?,
    val referer: String?,
    val esFavorito: Boolean = false,
    val oculto: Boolean = false,
    val orden: Int,
    val estado: EstadoCanal = EstadoCanal.SIN_VERIFICAR
)

enum class EstadoCanal {
    ACTIVO,
    CAIDO,
    SIN_VERIFICAR
}
