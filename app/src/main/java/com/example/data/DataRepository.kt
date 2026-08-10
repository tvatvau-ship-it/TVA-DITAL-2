package com.example.data

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DataRepository(private val database: AppDatabase) {

    // Canales
    fun getCanales() = database.canalDao().getAllCanales()
    fun getCanalesPorCategoria(catId: Int) = database.canalDao().getCanalesPorCategoria(catId)
    suspend fun insertCanal(canal: Canal) = database.canalDao().insertCanal(canal)
    suspend fun insertCanales(canales: List<Canal>) = database.canalDao().insertCanalesInTransaction(canales)
    
    suspend fun updateCanal(canal: Canal) = database.canalDao().updateCanal(canal)
    suspend fun deleteCanal(id: Int) = database.canalDao().deleteCanalById(id)
    suspend fun deleteAllCanales() = database.canalDao().deleteAllCanales()
    
    // Titulos
    fun getTitulos() = database.tituloDao().getAllTitulos()
    suspend fun getTituloById(id: Int) = database.tituloDao().getTituloById(id)
    suspend fun insertTitulo(titulo: Titulo) = database.tituloDao().insertTitulo(titulo)
    suspend fun updateTitulo(titulo: Titulo) = database.tituloDao().updateTitulo(titulo)
    suspend fun deleteTitulo(id: Int) = database.tituloDao().deleteTituloById(id)
    
    // Categorias
    fun getCategorias(tipo: TipoCategoria) = database.categoriaDao().getCategoriasPorTipo(tipo)
    suspend fun getSyncCategorias(tipo: TipoCategoria) = database.categoriaDao().getSyncCategorias(tipo)
    suspend fun insertCategoria(categoria: Categoria) = database.categoriaDao().insertCategoria(categoria)
    suspend fun insertCategorias(categorias: List<Categoria>) = database.categoriaDao().insertCategorias(categorias)
    suspend fun updateCategoria(categoria: Categoria) = database.categoriaDao().updateCategoria(categoria)
    suspend fun deleteCategoria(id: Int) = database.categoriaDao().deleteCategoriaById(id)
    suspend fun deleteCategoriasPorTipo(tipo: TipoCategoria) = database.categoriaDao().deleteCategoriasPorTipo(tipo)
}
