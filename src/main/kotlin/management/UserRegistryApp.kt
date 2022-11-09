package management

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration


class UserRegistryApp : Application<TestProjectConfiguration>() {
    override fun run(config: TestProjectConfiguration, env: Environment) {
//        val factory = JdbiFactory()
//        val jdbi: Jdbi = factory.build(env, config.getDataSourceFactory(), "h2")
//        val userDao: management.UserDAO = jdbi.onDemand(management.UserDAO::class.java)
//        env.jersey().register(management.UserResource(userDao, jdbi))

        val userResource = UserResource()
        env.jersey().register(userResource)
        env.healthChecks().register(
            "template",
            UserServiceHealthCheck(config.version)
        )
    }

    override fun initialize(bootstrap: Bootstrap<TestProjectConfiguration?>) {
        bootstrap.addBundle(object : SwaggerBundle<TestProjectConfiguration>() {
            override fun getSwaggerBundleConfiguration(configuration: TestProjectConfiguration): SwaggerBundleConfiguration? {
                return configuration.swaggerBundleConfiguration
            }
        })
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            UserRegistryApp().run(*args)
        }
    }
}