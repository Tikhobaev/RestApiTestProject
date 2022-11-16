import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import management.User
import management.UserCreation
import management.UserDAO
import management.UserResource
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

@ExtendWith(DropwizardExtensionsSupport::class)
class UserDaoTest() {
    @Test
    fun getUserByIdSuccess() {
        val found: User? = dao.findById(1)
        assertThat(found).isNotNull
        assertThat(found!!.copy(creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")))
            .isEqualTo(expectedUser)
        println("Passed")
    }
    @Test
    fun getUserByIdIncorrectId() {
        val createdUser: User? = dao.findById(3)
        assertThat(createdUser).isNull()
        println("Passed")
    }

    @Test
    fun getAllUsers() {
        val userCopy = user.copy(email = "newEmail@mail.com")
        val newId = dao.insert(userCopy)
        val newUser = dao.findById(newId)

        // Default params
        var users: List<User> = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(2)
        assertThat(users[0]).isNotNull

        // Offset
        users = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 1
        )
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0]).isEqualTo(newUser)

        // limit
        users = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 1,
            offset = 0
        )
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].copy(creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")))
            .isEqualTo(expectedUser)

        // sortBy
        users = dao.findAll(
            sortBy = UserResource.SortBy.email,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(2)
        assertThat(users[0].copy(creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z")))
            .isEqualTo(expectedUser)

        // sortOrder
        users = dao.findAll(
            sortBy = UserResource.SortBy.email,
            sortOrder = UserResource.SortOrder.DESC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(2)
        assertThat(users[0])
            .isEqualTo(newUser)

        dao.remove(newId)
        println("Passed")
    }

    @Test
    fun removeUser() {
        var users: List<User> = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(1)

        dao.remove(1)
        users = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(0)

        val oldUser: User? = dao.findById(1)
        assertThat(oldUser).isNotNull
        assertThat(oldUser!!.deletedTimestamp).isNotNull
        println("Passed")
    }

    @Test
    fun removeUserWithIncorrectId() {
        var users: List<User> = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(1)

        dao.remove(3)
        users = dao.findAll(
            sortBy = UserResource.SortBy.id,
            sortOrder = UserResource.SortOrder.ASC,
            limit = 25,
            offset = 0
        )
        assertThat(users.size).isEqualTo(1)
        println("Passed")
    }
    @Test
    fun updateUser() {
        val newFields = UserCreation("fn", "ln", "fnln@mail.com",
            LocalDate.of(2001, 12, 30)
        )
        dao.update(1, newFields)
        val updatedUser = dao.findById(1)!!.copy(creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z"))

        val updatedUserExpected = User(
            id = 1,
            firstName = newFields.firstName,
            lastName = newFields.lastName,
            email = newFields.email,
            birthDate = newFields.birthDate,
            creationTimestamp = updatedUser.creationTimestamp,
            deletedTimestamp = null
        )
        assertThat(updatedUser)
            .isEqualTo(updatedUserExpected)
        println("Passed")
    }

    companion object {
        private lateinit var jdbi: Jdbi
        private lateinit var dao: UserDAO
        private lateinit var user: UserCreation
        private lateinit var expectedUser: User
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            jdbi.installPlugin(SqlObjectPlugin())
                .installPlugin(KotlinPlugin())
                .installPlugin(KotlinSqlObjectPlugin())
            dao = jdbi.onDemand(UserDAO::class.java)
            dao.createUserTable()

            user = UserCreation(
                firstName = "Joe",
                lastName = "Biden",
                email = "jb@example.com",
                birthDate = LocalDate.of(2000,1, 1)
            )
            expectedUser = User(
                id = 1,
                firstName = "Joe",
                lastName = "Biden",
                email = "jb@example.com",
                birthDate = LocalDate.of(2000,1, 1),
                creationTimestamp = Instant.parse("2022-11-11T12:19:25.100Z"),
                deletedTimestamp = null
            )
            dao.insert(user)
        }
    }
}