package pl.kawaleria.auctsys.auctions.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class InappropriateContentException :
    ApiException(ServiceErrorResponseCode.AUCT07)