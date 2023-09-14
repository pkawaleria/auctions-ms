package pl.kawaleria.auctsys.auctions.domain

data class Category(
        val id: String,
        val name: String
)

data class CategoryPath(
        val pathElements: MutableList<Category>
) {
    fun lastCategory(): Category {
        return pathElements.last()
    }

    fun containsCategoryOfName(categoryName: String): Boolean {
        return pathElements.map { it.name }.contains(categoryName)
    }

    fun removeLast() {
        pathElements.removeLast()
    }

    fun remove(categoryName: String) {
        pathElements.removeIf { it.name == categoryName }
    }
}