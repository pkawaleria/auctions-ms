package pl.kawaleria.auctsys.categories.domain

import java.util.*

interface CategoryRepository {
    fun findSubcategories(categoryId: String): List<Category>
    fun getTopLevelOnes(): List<Category>
    fun getFinalCategories(): List<Category>
    fun findAllByNameContainingIgnoreCase(phrase: String): List<Category>
    fun delete(category: Category)
    fun existsById(categoryId: String): Boolean
    fun findById(categoryId: String): Optional<Category>
    fun save(category: Category): Category
    fun insert(category: Category): Category
    fun deleteAll()
    fun <S : Category?> saveAll(entities: MutableIterable<S>) : List<Category>
}