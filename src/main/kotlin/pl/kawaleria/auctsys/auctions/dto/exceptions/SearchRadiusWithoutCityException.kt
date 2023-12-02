package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class SearchRadiusWithoutCityException :
    ApiException(ServiceErrorResponseCode.AUCT10)