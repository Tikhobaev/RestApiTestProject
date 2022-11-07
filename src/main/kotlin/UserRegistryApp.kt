import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class UserRegistryApp : Application<TestProjectConfiguration>() {
    override fun run(config: TestProjectConfiguration, env: Environment) {
//        val factory = JdbiFactory()
//        val jdbi: Jdbi = factory.build(env, config.getDataSourceFactory(), "h2")
//        val userDao: UserDAO = jdbi.onDemand(UserDAO::class.java)
//        env.jersey().register(UserResource(userDao, jdbi))

        val userService = UserService()
        env.jersey().register(userService)
        env.healthChecks().register(
            "template",
            UserServiceHealthCheck(config.version)
        )
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            UserRegistryApp().run(*args)
        }
    }
}