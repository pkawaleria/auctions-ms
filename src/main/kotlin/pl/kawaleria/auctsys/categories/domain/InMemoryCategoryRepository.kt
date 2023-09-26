package pl.kawaleria.auctsys.categories.domain

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryCategoryRepository : CategoryRepository {
    val map: ConcurrentHashMap<String, Category> = ConcurrentHashMap()

    override fun findSubcategories(categoryId: String): List<Category> {
        return map.values.filter { it.parentCategoryId == categoryId }
    }

    override fun getTopLevelOnes(): List<Category> {
        return map.values.filter { it.isTopLevel }
    }

    override fun getFinalCategories(): List<Category> {
        return map.values.filter { it.isFinalNode }
    }

    override fun findAllByNameContainingIgnoreCase(phrase: String): List<Category> {
        return map.values.filter { it.name.contains(phrase, ignoreCase = true) }
    }

    override fun delete(category: Category) {
        map.remove(category.id)
    }

    override fun existsById(categoryId: String): Boolean {
        return map.containsKey(categoryId)
    }

    override fun findById(categoryId: String): Optional<Category> {
        return Optional.ofNullable(map[categoryId])
    }

    override fun save(category: Category): Category {
        map[category.id] = category
        return category
    }

    override fun insert(category: Category): Category {
        map[category.id] = category
        return category
    }

    override fun deleteAll() {
        map.clear()
    }

    override fun <S : Category?> saveAll(entities: MutableIterable<S>): List<Category> {
        return entities.map { save(it!!) }
    }
}