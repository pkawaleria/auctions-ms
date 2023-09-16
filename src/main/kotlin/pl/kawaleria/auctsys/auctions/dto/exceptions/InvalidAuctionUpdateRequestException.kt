package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class InvalidAuctionUpdateRequestException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid UpdateAuctionRequest")