package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class CanNotImportCitiesException :
    ApiException(ServiceErrorResponseCode.CIT02, HttpStatus.CONFLICT)