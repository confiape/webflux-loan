package loan.infraestructure.helpers

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JWTUtil {
    private val secret = "aVeryLongAndSecureSecretKeyThatHasMoreThanThirtyTwoCharacters!"

    private var key: Key? = null

    @PostConstruct
    fun init() {
        val decodedKey = Base64.getEncoder().encodeToString(secret.toByteArray())
        this.key = Keys.hmacShaKeyFor(decodedKey.toByteArray())

    }

    fun extractUsername(token: String?): String {
        return extractAllClaims(token).subject
    }

    fun extractExpiration(token: String?): Date {
        return extractAllClaims(token).expiration
    }

    private fun extractAllClaims(token: String?): Claims {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    }

    private fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun generateToken(username: String): String {
        val claims: Map<String, Any?> = HashMap()
        return createToken(claims, username)
    }

    private fun createToken(claims: Map<String, Any?>, subject: String): String {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(Date().time + 6000000)).signWith(key).compact()
    }

    fun validateToken(token: String?, username: String): Boolean {
        val extractedUsername = extractUsername(token)
        return (extractedUsername == username && !isTokenExpired(token))
    }
}
