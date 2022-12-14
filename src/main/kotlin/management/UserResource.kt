package management

import com.codahale.metrics.annotation.Timed
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.eclipse.jetty.http.HttpStatus
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/users")
@Api(value = "/users", tags = ["Users"])
@Produces("application/json")
class UserResource(private val dao: UserDAO) {
    @GET
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returns the user with specified id.", code = HttpStatus.OK_200, response = User::class)
    fun getUser(
        @ApiParam(value = "User ID", required = true)
        @PathParam("id")
        id: Int
    ): User {
        val foundUser = dao.findById(id)
            ?: throw WebApplicationException("User with id ($id) not found", Response.Status.NOT_FOUND)

        if (foundUser.deletedTimestamp != null) {
            throw WebApplicationException("User with id ($id) was deleted", Response.Status.NOT_FOUND)
        }
        return foundUser
    }

    @DELETE
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete user with specified id.", code = HttpStatus.OK_200, response = User::class)
    fun removeUser(@PathParam("id") id: Int): User {
        val foundUser = dao.findById(id)
            ?: throw WebApplicationException("User with id ($id) not found", Response.Status.NOT_FOUND)

        if (foundUser.deletedTimestamp != null) {
            throw WebApplicationException("User with id ($id) was deleted", Response.Status.NOT_FOUND)
        }

        dao.remove(id)
        return dao.findById(id)!!
    }

    enum class SortBy {
        id,
        firstName,
        lastName,
        email
    }

    enum class SortOrder {
        ASC,
        DESC
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Retrieve all users.",
        code = HttpStatus.OK_200,
        response = User::class,
        responseContainer = "List"
    )
    fun allUsers(
        @QueryParam("limit") @Min(1) @Max(100) @DefaultValue("25") limit: Int,
        @QueryParam("offset") @Min(0) @DefaultValue("0") offset: Int,
        @QueryParam("sortBy") @DefaultValue("id") sortBy: SortBy,
        @QueryParam("sortOrder") @DefaultValue("ASC") sortOrder: SortOrder,
        @QueryParam("showDeleted") @DefaultValue("false") showDeleted: Boolean,
    ): List<User> {
        return if (showDeleted) {
            dao.findAllEvenDeleted(sortBy, sortOrder, limit, offset)
        } else {
            dao.findAll(sortBy, sortOrder, limit, offset)
        }
    }

    @PUT
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Update user with specified id.",
        code = HttpStatus.OK_200,
        response = User::class
    )
    fun updateUser(@PathParam("id") id: Int, @Valid @NotNull user: UserCreation): User {
        val foundUser = dao.findById(id)
            ?: throw WebApplicationException("User with id ($id) not found", Response.Status.NOT_FOUND)

        if (foundUser.deletedTimestamp != null) {
            throw WebApplicationException("User with id ($id) was deleted", Response.Status.NOT_FOUND)
        }

        val foundByEmailUser = dao.findByEmail(user.email)
        if (foundByEmailUser != null && foundByEmailUser.id != id) {
            throw WebApplicationException("User with email (${user.email}) is already exist", Response.Status.CONFLICT)
        }

        dao.update(id, user)
        return dao.findById(id)!!
    }

    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Create user",
        code = HttpStatus.CREATED_201,
        response = User::class
    )
    fun createUser(@Valid @NotNull user: UserCreation): User {
        val foundByEmailUser = dao.findByEmail(user.email)
        if (foundByEmailUser != null) {
            throw WebApplicationException("User with email (${user.email}) is already exist", Response.Status.CONFLICT)
        }
        val id = dao.insert(user)
        return dao.findById(id)!!
    }
}