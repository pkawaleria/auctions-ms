package pl.kawaleria.auctsys.categories

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.requests.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.responses.CategoryPathResponse
import pl.kawaleria.auctsys.categories.dto.responses.CategoryResponse
import pl.kawaleria.auctsys.categories.dto.responses.CategorySearchResponse
import pl.kawaleria.auctsys.categories.dto.responses.CategoryWithPathResponse
import pl.kawaleria.auctsys.images.dto.responses.*
import java.util.*

@RestController
@RequestMapping("/auction-service/categories")
class CategoryController(private val categoryFacade: CategoryFacade) {

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun create(@RequestBody categoryRequest: CategoryCreateRequest): CategoryResponse = categoryFacade.create(request = categoryRequest)

    @GetMapping("/{categoryId}")
    fun get(@PathVariable categoryId: String): CategoryWithPathResponse = categoryFacade.get(categoryId = categoryId)

    @GetMapping("/{categoryId}/subcategories")
    fun getSubcategories(@PathVariable categoryId: String): List<CategoryWithPathResponse> = categoryFacade.getSubcategories(categoryId = categoryId)

    @GetMapping("/{categoryId}/path")
    fun getPath(@PathVariable categoryId: String): CategoryPathResponse = categoryFacade.getFullCategoryPath(categoryId = categoryId)

    @GetMapping("/entrypoints")
    @Operation(summary = "Retrieves top level categories which are roots (does not have parents)")
    fun getTopLevelCategories(): List<CategoryResponse> = categoryFacade.getTopLevelCategories()

    @GetMapping("/search")
    fun search(@RequestParam phraseInName: String): List<CategorySearchResponse> {
        return categoryFacade.search(phraseInName = phraseInName)
    }

    @GetMapping("/endpoints")
    @Operation(summary = "Retrieves the categories that are allowed to be assigned to auctions (some categories are to general to be assigned to auction)")
    fun getLowLevelCategories(): List<CategoryResponse> = categoryFacade.getFinalCategories()

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun delete(@PathVariable categoryId: String): ResponseEntity<Unit> {
        categoryFacade.delete(categoryId = categoryId)
        return ResponseEntity.noContent().build()
    }
}