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

object UserResource {
    private var conn: Connection

    init {
        Class.forName("org.h2.Driver")
        conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")
        val statement: Statement = conn.createStatement()
        statement.execute("drop table user if exists")
        statement.execute("create table user(id int primary key, firstName varchar(100), lastName varchar(100), email varchar(100))")
        statement.execute("insert into user values(1, 'UserName 1', 'UserSurname 1', 'email1@email.com')")
        statement.execute("insert into user values(2, 'UserName 2', 'UserSurname 2', 'email2@email.com')")
        statement.execute("insert into user values(3, 'UserName 3', 'UserSurname 3', 'email3@email.com')")
        statement.execute("insert into user values(4, 'UserName 4', 'UserSurname 4', 'email4@email.com')")
        val rs: ResultSet = statement.executeQuery("select * from user")
        statement.close()
    }

    fun getById(id: Int): User? {
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where id = $id")
        var user: User? = null

        if (rs.next()) {
            user = User(rs.getInt("id"), rs.getString("firstName"),
                        rs.getString("lastName"), rs.getString("email"))
            println("User successfully retrieved")
        } else {
            println("No user with id = $id")
        }
        statement.close()
        return user
    }

    fun all(): List<Any?> {
        val result: MutableList<User?> = ArrayList<User?>()
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user")
        var user: User? = null

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
        return count
    }

    fun remove(id: Int) {
        val statement = conn.prepareStatement("delete from user where id = $id")
        statement.executeUpdate()
        statement.close()
    }

    fun save(user: User): String {
        var result = ""
        val statement: Statement = conn.createStatement()
        val rs: ResultSet = statement.executeQuery("select * from user where id = ${user.id}")

        result = if (!rs.next()) {
            statement.execute("insert into user values(${user.id}, '${user.firstName}', '${user.lastName}', '${user.email}')")
            "Added User with id=" + user.id
        } else {
            statement.execute("update user " +
                    "set id = ${user.id}, firstName = '${user.firstName}', lastName = '${user.lastName}', email = '${user.email}'" +
                    "where id = ${user.id}"
            )
            "Updated User with id=" + user.id
        }
        statement.close()

        return result
    }
}

//class UserResource(userDao: UserDAO, jdbi: Jdbi) {
//    private var conn: Connection
//    private var userDao: UserDAO
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
//    fun getById(id: Int): User? {
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user where id = $id")
//        var user: User? = null
//
//        if (rs.next()) {
//            user = User(rs.getInt("id"), rs.getString("firstName"),
//                        rs.getString("lastName"), rs.getString("email"))
//            println("User successfully retrieved")
//        } else {
//            println("No user with id = $id")
//        }
//        statement.close()
//        return user
//    }
//
//    fun all(): List<Any?> {
//        val result: MutableList<User?> = ArrayList<User?>()
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user")
//        var user: User? = null
//
//        while (rs.next()) {
//            user = User(rs.getInt("id"), rs.getString("firstName"),
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
//    fun save(user: User): String {
//        var result = ""
//        val statement: Statement = conn.createStatement()
//        val rs: ResultSet = statement.executeQuery("select * from user where id = ${user.id}")
//
//        result = if (!rs.next()) {
//            statement.execute("insert into user values(${user.id}, '${user.firstName}', '${user.lastName}', '${user.email}')")
//            "Added User with id=" + user.id
//        } else {
//            statement.execute("update user " +
//                    "set id = ${user.id}, firstName = '${user.firstName}', lastName = '${user.lastName}', email = '${user.email}')" +
//                    "where id = ${user.id}"
//            )
//            "Updated User with id=" + user.id
//        }
//        statement.close()
//
//        return result
//    }
//}