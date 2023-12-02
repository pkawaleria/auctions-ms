package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class InvalidAuctionCreationRequestException(errorCodes: List<ServiceErrorResponseCode>) :
    ApiException(errorCodes)