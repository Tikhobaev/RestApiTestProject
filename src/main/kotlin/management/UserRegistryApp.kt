package management

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.kodein.di.Kodein
import org.kodein.di.direct
import java.time.Instant
import java.time.LocalDate
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton


class UserRegistryApp : Application<TestProjectConfiguration>() {
    override fun run(config: TestProjectConfiguration, env: Environment) {
        env.objectMapper.apply {
            registerModule(
                SimpleModule("SerializerDeserializerModule").also {
                    it.addSerializer(Instant::class.java, InstantSerializer())
                    it.addDeserializer(Instant::class.java, InstantDeserializer())
                    it.addSerializer(LocalDate::class.java, LocalDateSerializer())
                    it.addDeserializer(LocalDate::class.java, LocalDateDeserializer())
                }
            )
        }

        val kodein = Kodein {
            bind<TestProjectConfiguration>() with singleton { config }
            import(managementModule)
        }
        kodein.direct.instance<UserDAO>().createUserTable()

        env.jersey().register(kodein.direct.instance<UserResource>())
        env.healthChecks().register(
            "template",
            kodein.direct.instance<UserServiceHealthCheck>()
        )
    }

    override fun initialize(bootstrap: Bootstrap<TestProjectConfiguration?>) {
        bootstrap.addBundle(object : SwaggerBundle<TestProjectConfiguration>() {
            override fun getSwaggerBundleConfiguration(configuration: TestProjectConfiguration): SwaggerBundleConfiguration? {
                return configuration.swaggerBundleConfiguration
            }
        })
    }
    class InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
        override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.toString())
        }
    }

    class InstantDeserializer : StdDeserializer<Instant>(Instant::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
            return Instant.parse(p.text)
        }
    }

    class LocalDateSerializer : StdSerializer<LocalDate>(LocalDate::class.java) {
        override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.toString())
        }
    }

    class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
            return LocalDate.parse(p.text)
        }
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            UserRegistryApp().run(*args)
        }
    }
}