package loan.services


import loan.infraestructure.config.security.encodePassword
import loan.models.User
import loan.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono



@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun findByUsername(username: String): Mono<User> {
        return userRepository.findFirstByUsername(username)
    }

    fun save(user: User): Mono<User> {
        encodePassword(user.password) // Encrypt password before saving
        return userRepository.save(user)
    }
}
