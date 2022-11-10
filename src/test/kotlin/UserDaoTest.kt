import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import management.User
import management.UserCreation
import management.UserService
import org.assertj.core.api.Assertions
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DropwizardExtensionsSupport::class)
class DatabaseTest {
    val jdbi: Jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        UserService.initUserService(jdbi)
    }

    @Test
    fun createsFoo() {
        val mapper = Jackson.newObjectMapper()
        val user = mapper.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java)

        UserService.create(user)
        val createdUser: User? = UserService.getById(1)

        val expected: String = mapper.writeValueAsString(user)
        Assertions.assertThat(mapper.writeValueAsString(createdUser)).isEqualTo(expected)
    }
}