package loan.controllers


import loan.dtos.request.AuthRequest
import loan.dtos.request.UserRequest
import loan.dtos.response.AuthResponse
import loan.infraestructure.helpers.JWTUtil
import loan.infraestructure.config.security.encodePassword
import loan.models.User
import loan.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
class AuthController(
    private val jwtUtil: JWTUtil, private val userService: UserService
) {


    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthRequest): Mono<ResponseEntity<AuthResponse>> {
        return userService.findByUsername(authRequest.username).handle { it, sink ->
            if (it!!.password == authRequest.password) {
                sink.next(ResponseEntity.ok(AuthResponse(jwtUtil.generateToken(authRequest.username))))
            } else {
                sink.error(BadCredentialsException("Invalid username or password"))
            }
        }
            .switchIfEmpty(Mono.error<ResponseEntity<AuthResponse>>(BadCredentialsException("Invalid username or password")))
    }

    @PostMapping("/signup")
    fun signup(@RequestBody user: UserRequest): Mono<ResponseEntity<String?>> {

        encodePassword(user.password)
        return userService.save(User(
            password = user.password,
            username = user.username
        )).map {
            ResponseEntity.ok("User signed up successfully")
        }
    }

    @GetMapping("/protected")
    fun protectedEndpoint(): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity.ok("You have accessed a protected endpoint!"))
    }
}