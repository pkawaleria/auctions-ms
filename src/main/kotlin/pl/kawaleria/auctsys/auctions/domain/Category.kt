package pl.kawaleria.auctsys.auctions.domain

data class Category(
        val id: String,
        val name: String
)

data class CategoryPath(
        val pathElements: MutableList<Category>
) {
    fun lastCategory(): Category = pathElements.last()

    fun containsCategoryOfName(categoryName: String): Boolean = pathElements.map { it.name }.contains(categoryName)

    fun removeLast(): Category = pathElements.removeLast()

    fun remove(categoryName: String): Boolean = pathElements.removeIf { it.name == categoryName }
}