package pl.kawaleria.auctsys.categories.dto.responses

data class CategoryWithPathResponse (
        val id: String,
        val name: String,
        val isTopLevel: Boolean,
        val isFinalNode: Boolean,
        val description: String,
        val subcategories: List<SubcategoryResponse>,
        val path: SimpleCategoryPathResponse
)

