package pl.kawaleria.auctsys.configs


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
import org.springframework.web.filter.CorsFilter
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
            "/actuator/**",
            "/actuator",
            "/swagger-ui/**",
            "/swagger-ui",
            "/swagger-ui/*",
            "/v3/api-docs"
    )

    private val PUBLIC_ENDPOINTS = arrayOf(
            Endpoint(HttpMethod.GET, "/auction-service/auctions"),
            Endpoint(HttpMethod.GET, "/auction-service/auctions/**"),
            Endpoint(HttpMethod.GET, "/auction-service/auctions/search"),
            Endpoint(HttpMethod.GET, "/auction-service/cities/search"),
            Endpoint(HttpMethod.GET, "/auction-service/auctions/{auctionId}/images/{imageId}"),
            Endpoint(HttpMethod.GET, "/auction-service/auctions/{auctionId}/images"),
            Endpoint(HttpMethod.GET, "/auction-service/categories/**"),
            Endpoint(HttpMethod.GET, "/auction-service/users/{userId}/auctions"),
            Endpoint(HttpMethod.GET, "/auction-service/viewed-auctions/**"),
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        // State-less session (state in access-token only)
        http.sessionManagement { sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // Disable CSRF because of state-less session-management
        http.csrf { csrf -> csrf.disable() }

        // Enable and configure CORS
        http.cors { cors ->
            cors.configurationSource(corsConfigurationSource())
        }

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

    // TODO: limit this to only dev profile, create separate config for prod
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun corsFilter(): CorsFilter {
        return CorsFilter(corsConfigurationSource())
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

