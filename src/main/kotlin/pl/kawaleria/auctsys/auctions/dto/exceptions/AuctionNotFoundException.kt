package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class AuctionNotFoundException :
        ApiException(HttpStatus.NOT_FOUND.value(), "Accessed auction does not exist")