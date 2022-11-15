import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import management.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType


@ExtendWith(DropwizardExtensionsSupport::class)
internal class UserResourceTest {
    private val DAO: UserDAO = mockk<UserDAO>()
    private val EXT = ResourceExtension.builder()
        .addResource(UserResource(DAO))
        .build()
    private val user = User(
        id = 1,
        firstName = "Joe",
        lastName = "Biden",
        email = "jb@example.com",
        birthDate = LocalDate.of(2000,1, 1),
        creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z"),
        deletedTimestamp = null
    )
    private val userCreation = UserCreation(
        firstName = "Joe",
        lastName = "Biden",
        email = "jb@example.com",
        birthDate = LocalDate.of(2000,1, 1)

    )
    @Test
    fun getPersonSuccess() {
        every { DAO.findById(1) } returns user
        val found: User = EXT.target("/users/1").request().get(User::class.java)
        verify { DAO.findById(1) }
        assertThat(found).isEqualTo(user)
        println("Passed")
    }

    @Test
    fun getPersonFailed() {
        every { DAO.findById(2) } returns null
        val response = EXT.target("/users/2").request().get()
        verify { DAO.findById(2) }

        assertThat(response.status).isEqualTo(404)
        println("Passed")
    }

    @Test
    fun createUserSuccess() {
        every { DAO.findByEmail(any()) } returnsMany listOf(null, user)
        every { DAO.insert(userCreation) } returns 1
        every { DAO.findById(1) } returns user

        val entity = Entity.json(mapper.writeValueAsString(userCreation))
        val found = EXT.target("/users").request().post(entity, User::class.java)

        verify { DAO.findByEmail(any()) }
        verify { DAO.insert(userCreation) }
        verify { DAO.findById(1) }

        assertThat(found).isNotNull
        Assertions
            .assertThat(found)
            .isEqualTo(user)
        println("Passed")
    }

    @Test
    fun createUserConflict() {
        every { DAO.findByEmail(userCreation.email) } returns user

        val entity = Entity.json(mapper.writeValueAsString(userCreation))
        val response = EXT.target("/users").request().post(entity)
        verify { DAO.findByEmail(userCreation.email) }

        assertThat(response.status).isEqualTo(409)
        println("Passed")
    }

    @Test
    fun createUserViolateValidation() {
        val userIncorrectFnMin = userCreation.copy(firstName="")
        var response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userIncorrectFnMin)))
        assertThat(response.status).isEqualTo(422)

        val userIncorrectFnMax = userCreation.copy(firstName="a".repeat(101))
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userIncorrectFnMax)))
        assertThat(response.status).isEqualTo(422)


        val userIncorrectLnMin = userCreation.copy(lastName="")
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userIncorrectLnMin)))
        assertThat(response.status).isEqualTo(422)

        val userIncorrectLnMax = userCreation.copy(lastName="a".repeat(101))
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userIncorrectLnMax)))
        assertThat(response.status).isEqualTo(422)


        val userIncorrectEmail = userCreation.copy(email="jb.gmail.com")
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userIncorrectEmail)))
        assertThat(response.status).isEqualTo(422)

        val userTooShortEmail = userCreation.copy(email="")
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userTooShortEmail)))
        assertThat(response.status).isEqualTo(422)

        val userTooLongEmail = userCreation.copy(email="a".repeat(92) + "@mail.com")
        response = EXT.target("/users").request().post(Entity.json(mapper.writeValueAsString(userTooLongEmail)))
        assertThat(response.status).isEqualTo(422)

        val userIncorrectBd = "{\"firstName\":\"Joe\",\"lastName\":\"Biden\",\"email\":\"jb@example.com\",\"birthDate\":\"2000-13-32\"}"
        response = EXT.target("/users").request().post(Entity.json(userIncorrectBd))
        assertThat(response.status).isEqualTo(400)

        println("Passed")
    }

    @Test
    fun removeUserSuccess() {
        val userDeleted = user.copy(deletedTimestamp = Instant.now())
        every { DAO.findById(1) } returnsMany listOf(user, userDeleted)
        every { DAO.remove(1) } returns Unit

        val deleted = EXT.target("/users/1").request().delete(User::class.java)
        verify { DAO.findById(1) }
        verify { DAO.remove(1) }

        assertThat(deleted).isEqualTo(userDeleted)
        println("Passed")
    }

    @Test
    fun removeUserNotFound() {
        every { DAO.findById(1) } returns null
        val response = EXT.target("/users/1").request().delete()
        assertThat(response.status).isEqualTo(404)
        println("Passed")
    }

    @Test
    fun updateUserSuccess() {
        val userUpdated = userCreation.copy(firstName = "newFn")
        val expectedUser = user.copy(firstName = "newFn")
        every { DAO.findById(1) } returnsMany listOf(user, expectedUser)
        every { DAO.findByEmail(userUpdated.email) } returns null
        every { DAO.update(1, userUpdated) } returns Unit

        val entity = Entity.json(mapper.writeValueAsString(userUpdated))
        val updated = EXT.target("/users/1").request().put(entity, User::class.java)

        verify { DAO.findById(1) }
        verify { DAO.findByEmail(userUpdated.email) }
        verify { DAO.update(1, userUpdated) }

        assertThat(updated).isEqualTo(expectedUser)
        println("Passed")
    }

    @Test
    fun updateUserNotFound() {
        every { DAO.findById(1) } returns null
        val userUpdated = userCreation.copy(firstName = "newFn")
        val entity = Entity.json(mapper.writeValueAsString(userUpdated))
        val response = EXT.target("/users/1").request().put(entity)
        verify { DAO.findById(1) }
        assertThat(response.status).isEqualTo(404)
        println("Passed")
    }

    @Test
    fun updateUserConflict() {
        val userUpdated = userCreation.copy(firstName = "newFn")
        val conflictUser = user.copy(id = 2)
        every { DAO.findById(1) } returns user
        every { DAO.findByEmail(userUpdated.email) } returns conflictUser

        val entity = Entity.json(mapper.writeValueAsString(userUpdated))
        val response = EXT.target("/users/1").request().put(entity)

        verify { DAO.findById(1) }
        verify { DAO.findByEmail(userUpdated.email) }

        assertThat(response.status).isEqualTo(409)
        println("Passed")
    }

    @Test
    fun getAllUsersSuccess() {
        val userList = listOf(user)
        every {
            DAO.findAll(
                UserResource.SortBy.id,
                UserResource.SortOrder.ASC,
                25,
                0
            )
        } returns userList

        val foundUsers = EXT.target("/users/").request().get(object : GenericType<List<User>>() {})

        verify {
            DAO.findAll(
                UserResource.SortBy.id,
                UserResource.SortOrder.ASC,
                25,
                0
            )
        }

        assertThat(foundUsers).isEqualTo(userList)
        println("Passed")
    }

    @Test
    fun getAllUsersShowDeleted() {
        every {
            DAO.findAllEvenDeleted(
                UserResource.SortBy.id,
                UserResource.SortOrder.ASC,
                25,
                0,
            )
        } returns listOf(user)

        val foundUsers = EXT.target("/users/")
            .queryParam("showDeleted", true)
            .request()
            .get(object : GenericType<List<User>>() {})

        verify {
            DAO.findAllEvenDeleted(
                UserResource.SortBy.id,
                UserResource.SortOrder.ASC,
                25,
                0
            )
        }

        println("Passed")
    }

    @Test
    fun getAllUsersViolateValidation() {
        var response = EXT.target("/users/")
            .queryParam("limit", 0)
            .request().get()
        assertThat(response.status).isEqualTo(400)

        response = EXT.target("/users/")
            .queryParam("limit", 101)
            .request().get()
        assertThat(response.status).isEqualTo(400)

        response = EXT.target("/users/")
            .queryParam("offset", -1)
            .request().get()
        assertThat(response.status).isEqualTo(400)
        println("Passed")
    }


    companion object {
        private lateinit var mapper: ObjectMapper
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            mapper = Jackson.newObjectMapper()
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
        }
    }
}