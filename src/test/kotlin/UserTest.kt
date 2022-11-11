import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import io.dropwizard.jackson.Jackson.newObjectMapper
import management.User
import management.UserCreation
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate


internal class UserTest {
    @Test
    @Throws(Exception::class)
    fun seralizesToJSON() {
        val date = Instant.parse("2022-11-11T12:19:25.100Z")
        val user = User(1, "Joe", "Biden", "jb@example.com", date,
            LocalDate.of(2000, 1, 1), null
        )
        val expected: String = mapper.writeValueAsString(
            mapper.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java)
        )
        assertThat(mapper.writeValueAsString(user)).isEqualTo(expected)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun deserializesFromJSON() {
        val date = Instant.parse("2022-11-11T12:19:25.100Z")
        val user = User(1, "Joe", "Biden", "jb@example.com", date,
            LocalDate.of(2000, 1, 1), null
        )
        assertThat(mapper.readValue(javaClass.getResource("/fixtures/user.json"), User::class.java))
            .isEqualTo(user)
    }

    companion object {
        private lateinit var mapper: ObjectMapper
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            val kodein = Kodein {
                bind<ObjectMapper>() with singleton { newObjectMapper() }
            }
            mapper = kodein.instance()
            mapper.apply {
                registerModule(
                    SimpleModule("SerializerDeserializerModule").also {
                        it.addSerializer(Instant::class.java, InstantSerializer())
                        it.addDeserializer(Instant::class.java, InstantDeserializer())
                        it.addSerializer(LocalDate::class.java, LocalDateSerializer())
                        it.addDeserializer(LocalDate::class.java, LocalDateDeserializer())
                    }
                )
            }
        }
    }
}

internal class UserCreationTest {
    @Test
    @Throws(Exception::class)
    fun seralizesToJSON() {
        val user = UserCreation("Joe", "Biden", "jb@example.com", LocalDate.of(2000,1,1))
        val expected: String = MAPPER.writeValueAsString(
            MAPPER.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java)
        )
        assertThat(MAPPER.writeValueAsString(user)).isEqualTo(expected)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun deserializesFromJSON() {
        val user = UserCreation("Joe", "Biden", "jb@example.com", LocalDate.of(2000,1,1))
        assertThat(MAPPER.readValue(javaClass.getResource("/fixtures/userCreation.json"), UserCreation::class.java))
            .isEqualTo(user)
    }

    companion object {
        private val MAPPER: ObjectMapper = newObjectMapper()
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            MAPPER.apply {
                registerModule(
                    SimpleModule("SerializerDeserializerModule").also {
                        it.addSerializer(LocalDate::class.java, LocalDateSerializer())
                        it.addDeserializer(LocalDate::class.java, LocalDateDeserializer())
                    }
                )
            }
        }
    }
}

internal class InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

internal class InstantDeserializer : StdDeserializer<Instant>(Instant::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
        return Instant.parse(p.text)
    }
}

internal class LocalDateSerializer : StdSerializer<LocalDate>(LocalDate::class.java) {
    override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

internal class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
        return LocalDate.parse(p.text)
    }
}