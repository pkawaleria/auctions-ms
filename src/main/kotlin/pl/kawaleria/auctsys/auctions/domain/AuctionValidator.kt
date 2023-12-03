package pl.kawaleria.auctsys.auctions.domain

import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.dto.exceptions.InvalidAuctionCreationRequestException
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class AuctionValidator {
    fun validate(payload: CreateAuctionRequest) {
        validateAuction(payload.name, payload.description, payload.price)
    }

    fun validate(payload: UpdateAuctionRequest) {
        validateAuction(payload.name, payload.description, payload.price)
    }

    private fun validateAuction(name: String, description: String, price: Double) {
        val errorCodes = mutableListOf<ServiceErrorResponseCode>()

        if (!isValidName(name)) {
            errorCodes.add(ServiceErrorResponseCode.AUCT01)
        }
        if (!isValidDescription(description)) {
            errorCodes.add(ServiceErrorResponseCode.AUCT02)
        }
        if (!isValidPrice(price)) {
            errorCodes.add(ServiceErrorResponseCode.AUCT03)
        }

        if (errorCodes.isNotEmpty()) {
            throw InvalidAuctionCreationRequestException(errorCodes)
        }
    }

    private fun isValidName(name: String): Boolean = name.isNotBlank() && name.length in 5..100

    private fun isValidDescription(description: String): Boolean = description.isNotEmpty() && description.length in 20..500

    private fun isValidPrice(price: Double): Boolean = price > 0
}