package pl.kawaleria.auctsys.auctions

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.requests.*
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.commons.IpAddressResolver
import pl.kawaleria.auctsys.commons.toAuctioneerId

@RestController
@RequestMapping("/auction-service")
class AuctionCrudController(
    private val auctionFacade: AuctionFacade,
    private val ipAddressResolver: IpAddressResolver
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/auctions/search")
    fun searchAuctions(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) searchPhrase: String?,
        @RequestParam(required = false) categoryNamePhrase: String?,
        @RequestParam(required = false) categoryId: String?,
        @RequestParam(required = false) cityId: String?,
        @RequestParam(required = false) radius: Double?,
        @RequestParam(required = false) province: String?,
        @RequestParam(required = false) priceFrom: Int?,
        @RequestParam(required = false) priceTo: Int?,
        @RequestParam(required = false) sortBy: AuctionsSortBy?,
        @RequestParam(required = false) sortOrder: SortOrder?
    ): PagedAuctions {
        val pageRequest: PageRequest = PageRequest.of(page, pageSize)
        val searchRequest = AuctionsSearchRequest(
            searchPhrase = searchPhrase,
            categoryName = categoryNamePhrase,
            categoryId = categoryId,
            cityId = cityId,
            radius = radius,
            province = province,
            sortOrder = sortOrder,
            sortBy = sortBy,
            priceFrom = priceFrom,
            priceTo = priceTo
        )

        return auctionFacade.searchAuctions(searchRequest, pageRequest)
    }

    @GetMapping("/auctions/{auctionId}")
    fun getAuction(
            @PathVariable auctionId: String, request: HttpServletRequest
    ): AuctionDetailedResponse {
        val ipAddress: String = ipAddressResolver.getIpAddress(request)
        return auctionFacade.getAuctionDetails(auctionId, ipAddress)
    }

    @PutMapping("/auctions/{auctionId}/categories/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    fun changeCategory(
            @PathVariable auctionId: String,
            @PathVariable categoryId: String,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication)
            : AuctionDetailedResponse {
        return auctionFacade.changeCategory(auctionId, categoryId, authContext)
    }

    @GetMapping("/users/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionSimplifiedResponse> {
        return auctionFacade.findAuctionsByAuctioneer(userId)
    }

    @PostMapping("/auctions")
    @PreAuthorize("hasRole('ROLE_USER')")
    fun addAuction(
            @RequestBody payload: CreateAuctionRequest,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication,
    ): AuctionDetailedResponse {
        logger.info("Creating auction of request {$payload} for logged in auctioneer of id {${authContext.toAuctioneerId()}}")
        return auctionFacade.create(payload, authContext.toAuctioneerId())
    }

    @PutMapping("/auctions/{auctionId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    fun updateAuction(
            @PathVariable auctionId: String,
            @RequestBody payload: UpdateAuctionRequest,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication
    ): AuctionDetailedResponse {
        return auctionFacade.update(auctionId, payload, authContext)
    }

    @DeleteMapping("/auctions/{auctionId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    fun deleteAuction(
            @PathVariable auctionId: String,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication)
            : ResponseEntity<Unit> {
        auctionFacade.delete(auctionId, authContext)
        return ResponseEntity.noContent().build()
    }
}