package management

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin


class UserRegistryApp : Application<TestProjectConfiguration>() {
    override fun run(config: TestProjectConfiguration, env: Environment) {

        val jdbi: Jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        jdbi.installPlugin(SqlObjectPlugin())
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())

        env.jersey().register(UserResource(jdbi))
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