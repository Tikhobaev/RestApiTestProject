package management

import com.codahale.metrics.annotation.Timed
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.eclipse.jetty.http.HttpStatus
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/users")
@Api(value = "Users", description = "Users swagger", protocols = "http")
@Produces("application/json")
class UserResource {
    @GET
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returns the user with specified id.", code = HttpStatus.NOT_FOUND_404)
    fun getUser(
        @ApiParam(value = "User ID", required = true)
        @PathParam("id")
        id: Int
    ): User {
        val foundUser = UserService.getById(id)
        if (foundUser != null) {
            return foundUser
        }
        throw WebApplicationException(Response.Status.NOT_FOUND);
    }

    @DELETE
    @Timed
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete user with specified id.", code = HttpStatus.NOT_FOUND_404)
    fun removeUser(@PathParam("id") id: Int): Int {
        return UserService.remove(id)
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all users.")
    fun allUsers(): List<Any?> {
        return UserService.all()
    }

    @PUT
    @Timed
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Update user with specified id.",
        notes = "User info should contain valid id and new email should not belong to another user.",
        code = HttpStatus.CONFLICT_409
    )
    fun updateUser(user: User): String {
        return UserService.update(user)
    }

    @POST
    @Timed
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Create user",
        notes = "New email should not belong to another user.",
        code = HttpStatus.CONFLICT_409
    )
    fun createUser(user: UserCreation): String {
        return UserService.create(user)
    }

    // put
}