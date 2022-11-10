package management

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.bind
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import org.jdbi.v3.core.Jdbi
// import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import java.time.Instant
import java.time.LocalDate


class UserRegistryApp : Application<TestProjectConfiguration>() {
    override fun run(config: TestProjectConfiguration, env: Environment) {

        val jdbi: Jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        jdbi.installPlugin(SqlObjectPlugin())
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
        val dao = jdbi.onDemand(UserDAO::class.java)
        dao.createUserTable()

        // TODO remove
        dao.insert(UserCreation("UserName 1", "UserSurname 1", "email1@email.com", LocalDate.of(2000, 11, 10)))
        dao.insert(UserCreation("UserName 2", "UserSurname 2", "email2@email.com",  LocalDate.of(2000, 11, 11)))
        dao.insert(UserCreation("UserName 3", "UserSurname 3", "email3@email.com",  LocalDate.of(2000, 11, 12)))
        dao.insert(UserCreation("UserName 4", "UserSurname 4", "email4@email.com",  LocalDate.of(2000, 11, 13)))

        env.jersey().register(UserResource(dao))
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