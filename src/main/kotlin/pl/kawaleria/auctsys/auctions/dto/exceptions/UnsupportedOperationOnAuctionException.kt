package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

private const val MESSAGE_FORMAT = "Cannot perform %s on %s auction"

class UnsupportedOperationOnAuctionException(operation: String, currentState: String) :
        ApiException(HttpStatus.BAD_REQUEST.value(), String.format(MESSAGE_FORMAT, operation, currentState))