package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodios",
    foreignKeys = [
        ForeignKey(
            entity = Titulo::class,
            parentColumns = ["id"],
            childColumns = ["tituloId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Episodio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tituloId: Int,
    val temporada: Int,
    val numero: Int,
    val nombre: String,
    val streamUrl: String,
    val duracionVista: Long = 0L
)
