import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import management.*
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.GenericType


@ExtendWith(DropwizardExtensionsSupport::class)
internal class PersonResourceTest {
    private val DAO: UserDAO = mockk<UserDAO>()
    private val EXT = ResourceExtension.builder()
        .addResource(UserResource(DAO))
        .build()

    @Test
    fun getPersonSuccess() {
        every { DAO.findById(1) } returns user
        val found: User = EXT.target("/users/1").request().get(User::class.java)
        verify { DAO.findById(1) }

        val expectedUser: User = mapper.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java)
        assertThat(found.id).isEqualTo(expectedUser.id)
    }

    @Test
    fun getPersonFailed() {
        every { DAO.findById(2) } returns null
        var found: User? = null
        try {
            found = EXT.target("/users/2").request().get(User::class.java)
        } catch (e: WebApplicationException) {
            verify { DAO.findById(2) }
        }
        assertThat(found).isNull()
    }

    companion object {
        private lateinit var mapper: ObjectMapper
        private lateinit var user: User

        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            val kodein = Kodein {
                bind<ObjectMapper>() with singleton { Jackson.newObjectMapper() }
            }
            mapper = kodein.instance()
            mapper.apply {
                registerModule(
                    SimpleModule("SerializerDeserializerModule").also {
                        it.addSerializer(Instant::class.java, InstantSerializer())
                        it.addDeserializer(Instant::class.java, InstantDeserializer())
                        it.addSerializer(LocalDate::class.java, LocalDateSerializer())
                        it.addDeserializer(LocalDate::class.java, LocalDateDeserializer())
                    }
                )
            }
            user = mapper.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java)
        }
    }
}