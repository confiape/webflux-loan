package loan.controllers


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import loan.dtos.request.AuthRequest
import loan.dtos.request.TokenDto
import loan.dtos.request.UserRequest
import loan.dtos.response.AuthResponse
import loan.infraestructure.config.security.encodePassword
import loan.infraestructure.helpers.JWTUtil
import loan.models.User
import loan.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
class AuthController(
    private val jwtUtil: JWTUtil, private val userService: UserService,
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

    @PostMapping("/loginWithGoogleToken")
    fun loginWithGoogleToken(@RequestBody authRequest: TokenDto): Mono<ResponseEntity<AuthResponse>> =
        Mono.justOrEmpty(validateAsync(authRequest.accessToken)).flatMap { payload ->
            userService.findByUsername(payload.email)
                .map { ResponseEntity.ok(AuthResponse(jwtUtil.generateToken(payload.email))) }
        }.switchIfEmpty(Mono.error(BadCredentialsException("Invalid username or password")))


    fun validateAsync(jwt: String): GoogleIdToken.Payload? {
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
//        val clientSecrets = GoogleClientSecrets.load(
//            jsonFactory, InputStreamReader(`in`)
//        )
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), jsonFactory)
            .setAudience(Collections.singletonList("968556857827-vc4gsv2h1jlvkc8n2sceesmgb5uq9hht.apps.googleusercontent.com")).build()

        val idToken = verifier.verify(jwt)
        return idToken?.payload
    }

    @PostMapping("/signup")
    fun signup(@RequestBody user: UserRequest): Mono<ResponseEntity<String?>> {

        encodePassword(user.password)
        return userService.save(
            User(
                password = user.password, username = user.username
            )
        ).map {
            ResponseEntity.ok("User signed up successfully")
        }
    }

    @GetMapping("/protected")
    fun protectedEndpoint(): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity.ok("You have accessed a protected endpoint!"))
    }
}