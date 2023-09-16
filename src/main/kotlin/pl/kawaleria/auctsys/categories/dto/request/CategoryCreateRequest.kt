package pl.kawaleria.auctsys.categories.dto.request

data class CategoryCreateRequest(
        val name: String,
        val description: String,
        val parentCategoryId: String?,
        val isTopLevel: Boolean,
        val isFinalNode: Boolean
)