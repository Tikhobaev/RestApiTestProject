package management
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.Define
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface UserDAO {
    @SqlUpdate(
            "CREATE TABLE user(" +
            "id int NOT NULL AUTO_INCREMENT," +
            "firstName varchar(100)," +
            "lastName varchar(100)," +
            "email varchar(100) UNIQUE," +
            "creationTimestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "deletedTimestamp DATETIME," +
            "birthDate DATE," +
            "PRIMARY KEY (id));"
    )
    fun createUserTable()

    @GetGeneratedKeys
    @SqlUpdate("insert into user (firstName, lastName, email, birthDate) values(:firstName, :lastName, :email, :birthDate)")
    fun insert(@BindBean user: UserCreation): Int

    @SqlUpdate("update user set deletedTimestamp = CURRENT_TIMESTAMP where id = :id")
    fun remove(@Bind("id") id: Int)

    @SqlQuery("select * from user where id = :id")
    fun findById(@Bind("id") id: Int): User?

    @SqlQuery("select * from user where email = :email")
    fun findByEmail(@Bind("email") email: String): User?

    @SqlUpdate("update user set firstName = :firstName, lastName = :lastName, email = :email, birthDate = :birthDate where id = :id")
    fun update(@Bind("id") id: Int, @BindBean user: UserCreation)

//    @SqlQuery("select * from user " +
//            "where deletedTimestamp is null " +
//            "order by :sortBy :sortOrder " +
//            "limit :limit offset :offset")
    @SqlQuery(
        """
        select * from user
        where deletedTimestamp is null
        order by <sortBy> <sortOrder>
        limit :limit offset :offset
        """
    )
    fun findAll(
        @Define("sortBy") sortBy: UserResource.SortBy,
        @Define("sortOrder") sortOrder: UserResource.SortOrder,
        limit: Int,
        offset: Int,
    ): List<User>

    @SqlQuery(
        """
        select * from user
        order by <sortBy> <sortOrder>
        limit :limit offset :offset
        """
    )
    fun findAllEvenDeleted(
        @Define("sortBy") sortBy: UserResource.SortBy,
        @Define("sortOrder") sortOrder: UserResource.SortOrder,
        limit: Int,
        offset: Int,
    ): List<User>
}
