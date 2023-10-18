package pl.kawaleria.auctsys.categories.dto.responses

data class CategoryResponse(
        val id: String,
        val name: String,
        val isTopLevel: Boolean,
        val description: String,
        val subcategories: List<SubcategoryResponse>,
        val isFinalNode: Boolean
)

data class SubcategoryResponse(
        val id: String,
        val name: String
)

