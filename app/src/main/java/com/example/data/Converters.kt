package com.example.data

import androidx.room.TypeConverter
import com.example.data.model.EstadoCanal
import com.example.data.model.TipoCategoria
import com.example.data.model.TipoContenido

class Converters {
    @TypeConverter
    fun fromTipoCategoria(value: TipoCategoria) = value.name

    @TypeConverter
    fun toTipoCategoria(value: String) = enumValueOf<TipoCategoria>(value)

    @TypeConverter
    fun fromEstadoCanal(value: EstadoCanal) = value.name

    @TypeConverter
    fun toEstadoCanal(value: String) = enumValueOf<EstadoCanal>(value)

    @TypeConverter
    fun fromTipoContenido(value: TipoContenido) = value.name

    @TypeConverter
    fun toTipoContenido(value: String) = enumValueOf<TipoContenido>(value)
}
