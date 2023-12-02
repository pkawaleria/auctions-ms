package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class InvalidAuctionUpdateRequestException(errorCodes: List<ServiceErrorResponseCode>) :
    ApiException(errorCodes)