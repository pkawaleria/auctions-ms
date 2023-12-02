package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

private const val MESSAGE_FORMAT = "Cannot perform %s on %s auction"

class UnsupportedOperationOnAuctionException(operation: String, currentState: String) :
        ApiException(ServiceErrorResponseCode.AUCT11)