package loan.infraestructure.config.security


import loan.infraestructure.helpers.JWTUtil
import loan.services.UserService
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JWTAuthenticationManager(private val jwtUtil: JWTUtil, private val userService: UserService) :
    ReactiveAuthenticationManager {
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val token: String = authentication.credentials.toString()
        val username = jwtUtil.extractUsername(token)

        return userService.findByUsername(username).handle { it, sink ->
            if (jwtUtil.validateToken(token, it!!.username)) {
                sink.next(authentication)
            } else {
                sink.error(object : AuthenticationException("Invalid JWT token") {})
            }
        }
    }

    fun authenticationConverter(): ServerAuthenticationConverter {
        return ServerAuthenticationConverter { exchange ->
            val token = exchange.request.headers.getFirst("Authorization")
            if (token != null && token.startsWith("Bearer ")) {
                val jwtToken = token.substring(7)
                return@ServerAuthenticationConverter Mono.just(jwtToken).flatMap { jwt ->
                    val username = jwtUtil.extractUsername(jwt)
                    if (jwtUtil.validateToken(jwt, username)) {
                        val authentication = UsernamePasswordAuthenticationToken(username, jwt, emptyList())

                        Mono.just(authentication)
                    } else {
                        Mono.empty()
                    }
                }
            }
            Mono.empty()
        }
    }

}
