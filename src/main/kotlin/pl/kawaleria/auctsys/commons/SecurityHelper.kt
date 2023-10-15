package pl.kawaleria.auctsys.commons

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import pl.kawaleria.auctsys.configs.ROLE_PREFIX

@Component
class SecurityHelper {

    fun isCurrentUserAuthorizedForResource(authContext: Authentication, resourceOwnerId: String): Boolean {
        return authContext.toAuctioneerId() == resourceOwnerId ||
                authContext.toPlainTextRoles().contains("ROLE_ADMIN")
    }


    fun assertUserIsAuthorizedForResource(authContext: Authentication, resourceOwnerId: String) {
        if (!isCurrentUserAuthorizedForResource(authContext, resourceOwnerId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }
}

fun Authentication.toAuctioneerId(): String {
    return this.name.toString()
}

private fun Authentication.toPlainTextRoles(): List<String> {
    return this.authorities.map { it.authority }.filter { it.startsWith(ROLE_PREFIX) }
}