package pl.kawaleria.auctsys.categories.domain

class CategoryBuilder(private val categoryRepository: CategoryRepository) {

    private var currentCategory: Category? = null
    private val subCategories = mutableListOf<CategoryBuilder>()

    fun name(name: String): CategoryBuilder {
        currentCategory = Category(name = name, description = "", isFinalNode = false, isTopLevel = false)
        return this
    }

    fun description(description: String): CategoryBuilder {
        currentCategory?.description = description
        return this
    }

    fun topLevel(): CategoryBuilder {
        currentCategory?.isTopLevel = true
        return this
    }

    fun finalNode(): CategoryBuilder {
        currentCategory?.isFinalNode = true
        return this
    }

    fun subCategory(builder: CategoryBuilder): CategoryBuilder {
        subCategories.add(builder)
        return this
    }

    fun save(): Category? {
        val savedCategory: Category? = saveInternal()
        savedCategory?.let {
            subCategories.forEach { subCategoryBuilder ->
                subCategoryBuilder.currentCategory?.parentCategoryId = savedCategory.id
                subCategoryBuilder.save()
            }
        }
        return savedCategory
    }

    private fun saveInternal(): Category? {
        currentCategory?.let {
            val savedCategory: Category = categoryRepository.save(it)
            currentCategory = savedCategory // updating the currentCategory with the saved one
            return savedCategory
        }
        return null
    }
}
