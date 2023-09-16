package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class CanNotDeleteCitiesCollectionException:
    ApiException(HttpStatus.BAD_REQUEST.value(), "Can not delete cities collection")