package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class CanNotImportCitiesException :
    ApiException(HttpStatus.BAD_REQUEST.value(), "Can not import cities")