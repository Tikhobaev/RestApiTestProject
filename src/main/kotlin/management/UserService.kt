package management

import org.eclipse.jetty.http.HttpStatus
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.net.http.HttpRequest
import java.sql.ResultSet
import java.sql.SQLException

internal class UserMapper : RowMapper<User?> {
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext?): User {
        return User(
            rs.getInt("id"),
            rs.getString("firstName"),
            rs.getString("lastName"),
            rs.getString("email")
        )
    }
}

internal class UserCreationMapper : RowMapper<UserCreation?> {
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext?): UserCreation {
        return UserCreation(
            rs.getString("firstName"),
            rs.getString("lastName"),
            rs.getString("email")
        )
    }
}

@RegisterBeanMapper(UserMapper::class)
interface UserDAO {
    @SqlUpdate(
            "CREATE TABLE user(" +
            "id int NOT NULL AUTO_INCREMENT," +
            "firstName varchar(100)," +
            "lastName varchar(100)," +
            "email varchar(100)," +
            "PRIMARY KEY (id));"
    )
    fun createUserTable()

    @RegisterBeanMapper(UserCreationMapper::class)
    @SqlUpdate("insert into user (firstName, lastName, email) values(:firstName, :lastName, :email)")
    fun insert(@BindBean user: UserCreation)

    @SqlUpdate("delete from user where id = :id")
    fun remove(@Bind("id") id: Int)

    @SqlQuery("select * from user where id = :id")
    fun findById(@Bind("id") id: Int): User?

    @SqlQuery("select * from user where email = :email")
    fun findByEmail(@Bind("email") email: String): User?

    @SqlUpdate("update user set firstName = :firstName, lastName = :lastName, email = :email where id = :id")
    fun update(@BindBean user: User)

    @SqlQuery("select * from user")
    fun findAll(): List<User>

    @SqlQuery("select COUNT(*) from user")
    fun countAll(): Int
}

object UserService { // service
    private lateinit var jdbi: Jdbi
    private lateinit var dao: UserDAO
    fun initUserService(jdbi: Jdbi) {
        this.jdbi = jdbi
        dao = jdbi.onDemand(UserDAO::class.java)
        dao.createUserTable()
        dao.insert(UserCreation("UserName 1", "UserSurname 1", "email1@email.com"))
        dao.insert(UserCreation("UserName 2", "UserSurname 2", "email2@email.com"))
        dao.insert(UserCreation("UserName 3", "UserSurname 3", "email3@email.com"))
        dao.insert(UserCreation("UserName 4", "UserSurname 4", "email4@email.com"))
    }
    fun getById(id: Int): User? {
        return dao.findById(id)
    }

    fun getCount(): Int {
        return dao.countAll()
    }
    fun all(): List<User> {
        return dao.findAll()
   }

    fun remove(id: Int): Int {
        val foundUser = dao.findById(id) ?: return HttpStatus.NOT_FOUND_404
        dao.remove(id)
        return HttpStatus.OK_200
    }

    fun update(user: User): Int {
        val foundByIdUser = dao.findById(user.id) ?: return HttpStatus.NOT_FOUND_404
        val foundByEmailUser = dao.findByEmail(user.email)
        if (foundByEmailUser != null) {
            return HttpStatus.CONFLICT_409
        }

        dao.update(user)
        return HttpStatus.OK_200
    }

    fun create(user: UserCreation): Int {
        val foundByEmailUser = dao.findByEmail(user.email)
        if (foundByEmailUser != null) {
            return HttpStatus.CONFLICT_409
        }

        dao.insert(user)
        return HttpStatus.OK_200
    }
}
