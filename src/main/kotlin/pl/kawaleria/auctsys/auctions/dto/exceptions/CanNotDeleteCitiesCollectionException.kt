package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class CanNotDeleteCitiesCollectionException:
    ApiException(HttpStatus.BAD_REQUEST.value(), "Can not delete cities collection")