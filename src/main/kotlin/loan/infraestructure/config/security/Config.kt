package loan.infraestructure.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JWTAuthenticationManager
) {


    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationManager.authenticationConverter())

        return http.csrf {
            it.disable()
        }.authorizeExchange {
            it.pathMatchers("/login",
                "/signup",
                "/webjars/**",
                "/v3/**").permitAll()
                .anyExchange().authenticated()
        }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout{it.disable()}
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION).build()
    }
}

fun encodePassword(password: String) {
    BCryptPasswordEncoder().encode(password)
}