package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class CityNotFoundException :
    ApiException(HttpStatus.NOT_FOUND.value(), "Accessed city does not exist")