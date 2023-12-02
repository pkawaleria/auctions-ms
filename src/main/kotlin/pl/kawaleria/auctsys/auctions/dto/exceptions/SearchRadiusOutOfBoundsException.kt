package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class SearchRadiusOutOfBoundsException :
    ApiException(ServiceErrorResponseCode.AUCT09)