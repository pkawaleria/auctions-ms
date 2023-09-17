package pl.kawaleria.auctsys.configs

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException


private const val BEARER_PREFIX = "Bearer"

private const val ROLES_KEY_IN_JWT = "roles"

private const val ROLE_PREFIX = "ROLE_"

@Component
class JwtAuthenticationFilter(private val jwtDecoder: JwtDecoder) : OncePerRequestFilter() {


    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest,
                                  res: HttpServletResponse,
                                  chain: FilterChain) {
        val header: String? = req.getHeader(HttpHeaders.AUTHORIZATION)
        if (header.isNullOrBlank() || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(req, res)
            return
        }
        val authentication = getAuthentication(req)
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        val jws = token.replace(BEARER_PREFIX, "")
        val decodedJwt = jwtDecoder.decode(jws)
        val userId: String = decodedJwt.subject
        val roles: List<GrantedAuthority> = decodedJwt.getClaimAsStringList(ROLES_KEY_IN_JWT)
                .map { SimpleGrantedAuthority("$ROLE_PREFIX$it") }

        return UsernamePasswordAuthenticationToken(userId, "", roles)
    }
}