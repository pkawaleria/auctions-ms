package pl.kawaleria.auctsys.configs

//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
//import org.springframework.security.web.SecurityFilterChain
//import org.springframework.web.cors.CorsConfiguration
//import org.springframework.web.cors.CorsConfigurationSource
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource
//
//@Configuration
//@EnableWebSecurity
//class SecurityConfig {
//    private val AUTH_WHITELIST = arrayOf(
//        "/swagger-resources",
//        "/swagger-resources/**",
//        "/configuration/ui",
//        "/configuration/security",
//        "/swagger-ui.html",
//        "/webjars/**",
//        "/v3/api-docs/**",
//        "/api/public/**",
//        "/api/public/authenticate",
//        "/actuator/*",
//        "/swagger-ui/**"
//    )
//
//    @Bean
//    fun filterChain(http: HttpSecurity): SecurityFilterChain {
//        http.authorizeHttpRequests { authorize ->
//            AUTH_WHITELIST.forEach { path ->
//                authorize.requestMatchers(path).permitAll()
//            }
//
//            authorize.anyRequest().permitAll()
//        }
//
//        return http.build()
//    }
//
//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
//        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
//        configuration.allowedHeaders = listOf("authorization", "content-type")
//        configuration.allowCredentials = true
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", configuration)
//        return source
//    }
//}