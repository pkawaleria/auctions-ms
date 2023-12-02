package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class CityNotFoundException :
    ApiException(ServiceErrorResponseCode.CIT01, HttpStatus.NOT_FOUND)