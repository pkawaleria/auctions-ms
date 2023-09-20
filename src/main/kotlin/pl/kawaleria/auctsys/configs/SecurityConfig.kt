package pl.kawaleria.auctsys.configs


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


private const val JWT_SIGNING_ALGORITHM = "HmacSHA256"
const val ROLES_KEY_IN_JWT = "roles"
const val ROLE_PREFIX = "ROLE_"

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {


    @Value("\${jwt.signing.key}")
    private val jwtSigningKey: String = ""

    private val AUTH_WHITELIST = arrayOf(
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/v3/api-docs/**",
        "/api/public/**",
        "/api/public/authenticate",
        "/actuator/*",
        "/swagger-ui/**"
    )

    private val PUBLIC_ENDPOINTS = arrayOf(
        Endpoint(HttpMethod.GET, "/auction-service/auctions"),
        Endpoint(HttpMethod.GET, "/auction-service/auctions/{auctionId}"),
        Endpoint(HttpMethod.GET, "/auction-service/auctions/search"),
        Endpoint(HttpMethod.GET, "/cities/search"),
        Endpoint(HttpMethod.GET, "/auction-service/auctions/{auctionId}/images/{imageId}"),
        Endpoint(HttpMethod.GET, "/auction-service/auctions/{auctionId}/images"),
        Endpoint(HttpMethod.GET, "/auction-service/categories/**"),
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            AUTH_WHITELIST.forEach { path ->
                authorize.requestMatchers(path).permitAll()
            }
            PUBLIC_ENDPOINTS.forEach { endpoint ->
                authorize.requestMatchers(endpoint.method, endpoint.pattern).permitAll()
            }
            authorize.anyRequest().authenticated()
        }

        http.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt ->
                jwt.decoder(jwtDecoder())
                    .jwtAuthenticationConverter(CustomAuthenticationConverter())
            }
        }
        return http.build()
    }



    @Bean
    fun jwtDecoder(): JwtDecoder {
        val originalKey: SecretKey = SecretKeySpec(jwtSigningKey.toByteArray(), JWT_SIGNING_ALGORITHM)
        return NimbusJwtDecoder.withSecretKey(originalKey).build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        configuration.allowedHeaders = listOf("authorization", "content-type")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    internal class CustomAuthenticationConverter : Converter<Jwt, JwtAuthenticationToken> {
        override fun convert(jwt: Jwt): JwtAuthenticationToken {
            val userId: String = jwt.subject
            val roles: List<GrantedAuthority> = jwt.getClaimAsStringList(ROLES_KEY_IN_JWT)
                .map { SimpleGrantedAuthority("$ROLE_PREFIX$it") }
            return JwtAuthenticationToken(jwt, roles, userId)
        }
    }

    internal data class Endpoint(val method: HttpMethod, val pattern: String)
}

