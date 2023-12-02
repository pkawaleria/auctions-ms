package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class ExpiredAuctionException :
    ApiException(ServiceErrorResponseCode.AUCT06)