package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class AuctionNotFoundException :
        ApiException(ServiceErrorResponseCode.AUCT04, HttpStatus.NOT_FOUND)