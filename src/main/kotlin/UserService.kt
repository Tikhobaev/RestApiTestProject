import com.codahale.metrics.annotation.Timed
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/user")
class UserService {
    @GET
    @Timed
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUser(@PathParam("id") id: Int): User? {
        return UserResource.getById(id)
    }

    @GET
    @Timed
    @Path("/remove/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    fun removeUser(@PathParam("id") id: Int): String {
        UserResource.remove(id)
        return "User with id = $id removed. Total count: " + UserResource.getCount()
    }

    @get:Produces(MediaType.APPLICATION_JSON)
    @get:Path("/all")
    @get:Timed
    @get:GET
    val users: List<Any?>
        get() = UserResource.all()

    @POST
    @Timed
    @Path("/save")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    fun addUser(user: User): String {
        return UserResource.save(user)
    }
}