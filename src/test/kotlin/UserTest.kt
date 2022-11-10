import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.jackson.Jackson.newObjectMapper
import management.User
import management.UserCreation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class UserTest {
    @Test
    @Throws(Exception::class)
    fun seralizesToJSON() {
        val user = User(1, "Joe", "Biden", "jb@example.com")
        val expected: String = MAPPER.writeValueAsString(
            MAPPER.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java)
        )
        assertThat(MAPPER.writeValueAsString(user)).isEqualTo(expected)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun deserializesFromJSON() {
        val user = User(1, "Joe", "Biden", "jb@example.com")
        assertThat(MAPPER.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java))
            .isEqualTo(user)
    }

    companion object {
        private val MAPPER: ObjectMapper = newObjectMapper()
    }
}

internal class UserCreationTest {
    @Test
    @Throws(Exception::class)
    fun seralizesToJSON() {
        val user = UserCreation("Joe", "Biden", "jb@example.com")
        val expected: String = MAPPER.writeValueAsString(
            MAPPER.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java)
        )
        assertThat(MAPPER.writeValueAsString(user)).isEqualTo(expected)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun deserializesFromJSON() {
        val user = UserCreation("Joe", "Biden", "jb@example.com")
        assertThat(MAPPER.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java))
            .isEqualTo(user)
    }

    companion object {
        private val MAPPER: ObjectMapper = newObjectMapper()
    }
}