package management

import com.sun.research.ws.wadl.HTTPMethods
import org.eclipse.jetty.http.HttpStatus
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


interface UserDAO {
    @SqlUpdate("create table user(id int primary key, firstName varchar(100), lastName varchar(100), email varchar(100))")
    fun createUserTable()

    @SqlUpdate("insert into user values (:id, :firstName, :lastName, :email)")
    fun insert(@Bind("id") id: Int, @Bind("firstName") firstName: String?,
               @Bind("lastName") lastName: String?, @Bind("email") email: String?)

    @SqlQuery("select * from user where id = :id")
    fun findById(@Bind("id") id: Int): String?
}

object UserService { // service
    private var conn: Connection

    init {
        Class.forName("org.h2.Driver")
        conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")
        val statement: Statement = conn.createStatement()
        statement.execute("drop table user if exists")
        statement.execute("CREATE TABLE user(" +
                              "id int NOT NULL AUTO_INCREMENT," +
                              "firstName varchar(100)," +
                              "lastName varchar(100)," +
                              "email varchar(100)," +
                              "PRIMARY KEY (id))")
        statement.execute("insert into user (firstName, lastName, email) values('UserName 1', 'UserSurname 1', 'email1@email.com')")
        statement.execute("insert into user (firstName, lastName, email) values('UserName 2', 'UserSurname 2', 'email2@email.com')")
        statement.execute("insert into user (firstName, lastName, email) values('UserName 3', 'UserSurname 3', 'email3@email.com')")
        statement.execute("insert into user (firstName, lastName, email) values('UserName 4', 'UserSurname 4', 'email4@email.com')")
        statement.close()
    }

    fun getById(id: Int): User? {
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where id = $id")
        var user: User? = null

        if (rs.next()) {
            user = User(rs.getInt("id"), rs.getString("firstName"),
                        rs.getString("lastName"), rs.getString("email"))
        }
        statement.close()
        return user
    }

    fun all(): List<Any?> {
        val result: MutableList<User?> = ArrayList<User?>()
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user")
        var user: User

        while (rs.next()) {
            user = User(rs.getInt("id"), rs.getString("firstName"),
                        rs.getString("lastName"), rs.getString("email"))
            result.add(user)
        }
        statement.close()
        return result
    }

    fun getCount(): Int {
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select count(*) from user")
        var count = -1
        if (rs.next()) {
            count = rs.getInt(1)
        }
        statement.close()
        return count
    }

    fun remove(id: Int): Int {
        var result = HttpStatus.NOT_FOUND_404
        var statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where id = '$id'")
        val notFound = !rs.next()
        statement.close()

        if (notFound) {
            return result
        }

        statement = conn.prepareStatement("delete from user where id = $id")
        statement.executeUpdate()
        statement.close()
        return HttpStatus.OK_200
    }

    fun update(user: User): String {
        var result = HttpStatus.NOT_FOUND_404.toString()
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where id = '${user.id}'")

        if (rs.next()) {
            val usersWithSameEmail: ResultSet = statement.executeQuery("select * from user where email = '${user.email}'")
            result = if (usersWithSameEmail.next()) {
                HttpStatus.CONFLICT_409.toString()
            } else {
                statement.execute(
                    "update user " +
                            "set firstName = '${user.firstName}', lastName = '${user.lastName}', email = '${user.email}'" +
                            "where id = ${user.id}"
                )
                HttpStatus.OK_200.toString()
            }
        }
        statement.close()

        return result
    }

    fun create(user: UserCreation): String {
        var result = HttpStatus.CONFLICT_409.toString()
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where email = '${user.email}'")

        if (!rs.next()) {
            statement.execute("insert into user (firstName, lastName, email) values('${user.firstName}', '${user.lastName}', '${user.email}')")
            result = HttpStatus.CREATED_201.toString()
        }
        statement.close()

        return result
    }
}

//class management.UserResource(userDao: management.UserDAO, jdbi: Jdbi) {
//    private var conn: Connection
//    private var userDao: management.UserDAO
//    private var jdbi: Jdbi
//
//    init {
//        this.userDao = userDao
//        this.jdbi = jdbi
//        jdbi.open()
//        userDao.createUserTable()
//        Class.forName("org.h2.Driver")
//        conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")
//        val statement: Statement = conn.createStatement()
//        statement.execute("drop table user if exists")
//        statement.execute("create table user(id int primary key, firstName varchar(100), lastName varchar(100), email varchar(100))")
//        statement.execute("insert into user values(1, 'UserName 1', 'UserSurname 1', 'email1@email.com')")
//        statement.execute("insert into user values(2, 'UserName 2', 'UserSurname 2', 'email2@email.com')")
//        statement.execute("insert into user values(3, 'UserName 3', 'UserSurname 3', 'email3@email.com')")
//        statement.execute("insert into user values(4, 'UserName 4', 'UserSurname 4', 'email4@email.com')")
//        val rs: ResultSet = statement.executeQuery("select * from user")
//        statement.close()
//    }
//
//    fun getById(id: Int): management.User? {
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user where id = $id")
//        var user: management.User? = null
//
//        if (rs.next()) {
//            user = management.User(rs.getInt("id"), rs.getString("firstName"),
//                        rs.getString("lastName"), rs.getString("email"))
//            println("management.User successfully retrieved")
//        } else {
//            println("No user with id = $id")
//        }
//        statement.close()
//        return user
//    }
//
//    fun all(): List<Any?> {
//        val result: MutableList<management.User?> = ArrayList<management.User?>()
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user")
//        var user: management.User? = null
//
//        while (rs.next()) {
//            user = management.User(rs.getInt("id"), rs.getString("firstName"),
//                        rs.getString("lastName"), rs.getString("email"))
//            result.add(user)
//        }
//        statement.close()
//        return result
//    }
//
//    fun getCount(): Int {
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select count(*) as total from user")
//        return rs.getInt("total")
//    }
//
//    fun remove(id: Int) {
//        val statement = conn.prepareStatement("delete from user where id = $id")
//        statement.executeUpdate()
//        statement.close()
//    }
//
//    fun save(user: management.User): String {
//        var result = ""
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user where id = ${user.id}")
//
//        result = if (!rs.next()) {
//            statement.execute("insert into user values(${user.id}, '${user.firstName}', '${user.lastName}', '${user.email}')")
//            "Added management.User with id=" + user.id
//        } else {
//            statement.execute("update user " +
//                    "set id = ${user.id}, firstName = '${user.firstName}', lastName = '${user.lastName}', email = '${user.email}')" +
//                    "where id = ${user.id}"
//            )
//            "Updated management.User with id=" + user.id
//        }
//        statement.close()
//
//        return result
//    }
//}