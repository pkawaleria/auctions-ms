package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class CanNotImportCitiesException :
    ApiException(HttpStatus.BAD_REQUEST.value(), "Can not import cities")