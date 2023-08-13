package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class CategoryNotFound :
        ApiException(HttpStatus.NOT_FOUND.value(), "Accessed category does not exist")