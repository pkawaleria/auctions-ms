package pl.kawaleria.auctsys.dtos

import pl.kawaleria.auctsys.models.Category
import org.springframework.web.server.ResponseStatusException

class ApiException(code: Int, message: String): ResponseStatusException(code, message, null)
data class AuctionDto(
    val id: String?,
    val name: String?,
    val category: Category?,
    val description: String?,
    val price: Double?,
    val auctioneerId: String?
)