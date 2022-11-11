import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import management.User
import management.UserCreation
import management.UserDAO
import org.assertj.core.api.Assertions
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

@ExtendWith(DropwizardExtensionsSupport::class)
class UserDaoTest() {
    @Test
    fun createsUser() {
        val createdUser: User? = dao.findById(1)
        Assertions.assertThat(createdUser).isNotNull
        createdUser!!.creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")
        Assertions.assertThat(mapper.writeValueAsString(createdUser)).isEqualTo(mapper.writeValueAsString(expectedUser))
        println("Passed")
    }

    @Test
    fun getUserByIdIncorrectId() {
        val createdUser: User? = dao.findById(2)
        Assertions.assertThat(createdUser).isNull()
        println("Passed")
    }

    @Test
    fun getAllUsers() {
        val users: List<User> = dao.findAll()
        Assertions.assertThat(users.size).isEqualTo(1)
        Assertions.assertThat(users[0]).isNotNull
        users[0].creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")
        Assertions.assertThat(mapper.writeValueAsString(users[0])).isEqualTo(mapper.writeValueAsString(expectedUser))
        println("Passed")
    }

    @Test
    fun removeUser() {
        var users: List<User> = dao.findAll()
        Assertions.assertThat(users.size).isEqualTo(1)

        dao.remove(1)
        users = dao.findAll()
        Assertions.assertThat(users.size).isEqualTo(0)

        val oldUser: User? = dao.findById(1)
        Assertions.assertThat(oldUser).isNotNull
        Assertions.assertThat(oldUser!!.deletedTimestamp).isNotNull()
        println("Passed")
    }

    @Test
    fun removeUserWithIncorrectId() {
        var users: List<User> = dao.findAll()
        Assertions.assertThat(users.size).isEqualTo(1)

        dao.remove(2)
        users = dao.findAll()
        Assertions.assertThat(users.size).isEqualTo(1)
        println("Passed")
    }
    @Test
    fun updateUser() {
        val newFields = UserCreation("fn", "ln", "fnln@mail.com",
            LocalDate.of(2001, 12, 30)
        )
        dao.update(1, newFields)
        val updatedUser = dao.findById(1)
        Assertions.assertThat(updatedUser).isNotNull
        updatedUser!!.creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")

        val updatedUserExpected = mapper.readValue(
            javaClass.getResource("/fixtures/updatedUser.json"),
            User::class.java
        )
        Assertions
            .assertThat(mapper.writeValueAsString(updatedUser))
            .isEqualTo(mapper.writeValueAsString(updatedUserExpected))
        println("Passed")
    }

    companion object {
        private lateinit var jdbi: Jdbi
        private lateinit var dao: UserDAO
        private lateinit var mapper: ObjectMapper
        private lateinit var user: UserCreation
        private lateinit var expectedUser: User
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            val kodein = Kodein {
                bind<Jdbi>() with singleton {Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")}
                bind<ObjectMapper>() with singleton {Jackson.newObjectMapper()}
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

            jdbi = kodein.instance()
            jdbi.installPlugin(SqlObjectPlugin())
                .installPlugin(KotlinPlugin())
                .installPlugin(KotlinSqlObjectPlugin())
            dao = jdbi.onDemand(UserDAO::class.java)
            dao.createUserTable()
            user = mapper.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java)
            expectedUser = mapper.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java)
            dao.insert(user)
        }
    }
}