package pl.kawaleria.auctsys.categories.dto.requests

data class CategoryCreateRequest(
        val name: String,
        val description: String,
        val parentCategoryId: String?,
        val isTopLevel: Boolean,
        val isFinalNode: Boolean
)