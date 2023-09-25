package pl.kawaleria.auctsys

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

const val AUCTIONEER_ID_UNDER_TEST = "auctioneer-id"

const val ADMIN_ID_UNDER_TEST = "admin-id"

fun MockHttpServletRequestBuilder.withAuthenticatedAdmin(): MockHttpServletRequestBuilder {
    return this.with(
        SecurityMockMvcRequestPostProcessors.jwt()
            .jwt { jwt -> jwt.subject(ADMIN_ID_UNDER_TEST) }
            .authorities(listOf(SimpleGrantedAuthority("ROLE_ADMIN"))))

}
fun MockHttpServletRequestBuilder.withAnonymousUser(): MockHttpServletRequestBuilder {
    return this.with(
        SecurityMockMvcRequestPostProcessors.anonymous()
    )
}
fun MockHttpServletRequestBuilder.withAuthenticatedAuctioneer(): MockHttpServletRequestBuilder {
    return this.with(
        SecurityMockMvcRequestPostProcessors.jwt()
            .jwt { jwt -> jwt.subject(AUCTIONEER_ID_UNDER_TEST) }
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER"))))

}

class TestAuctioneerAuthentication: Authentication {
    override fun getName(): String {
        return AUCTIONEER_ID_UNDER_TEST
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return arrayListOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getCredentials(): Any {
        return ""
    }

    override fun getDetails(): Any {
        return ""
    }

    override fun getPrincipal(): Any {
        return AUCTIONEER_ID_UNDER_TEST
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
    }

}