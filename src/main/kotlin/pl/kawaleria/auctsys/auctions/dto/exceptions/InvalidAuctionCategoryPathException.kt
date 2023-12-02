package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class InvalidAuctionCategoryPathException :
    ApiException(ServiceErrorResponseCode.AUCT08)