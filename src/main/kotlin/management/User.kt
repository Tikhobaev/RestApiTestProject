package management

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModelProperty
import java.time.Instant
import java.time.LocalDate
import javax.validation.constraints.*

private const val ID_HELP = "User unique identifier"
private const val ID_EXAMPLE = "1"

private const val FIRST_NAME_HELP = "User first name"
private const val FIRST_NAME_EXAMPLE = "Joe"

private const val LAST_NAME_HELP = "User last name"
private const val LAST_NAME_EXAMPLE = "Biden"

private const val EMAIL_HELP = "User email. Must be unique"
private const val EMAIL_EXAMPLE = "joe.biden@gmail.com"

private const val CREATION_TIMESTAMP_HELP = "Timestamp of user creation"
private const val CREATION_TIMESTAMP_EXAMPLE = "1999-11-10 23:30:16.57"

private const val BIRTH_DATE_HELP = "User birth time"
private const val BIRTH_DATE_EXAMPLE = "2000-05-30"

data class User(
    @JsonProperty("id")
    @ApiModelProperty(
        value = ID_HELP,
        example = ID_EXAMPLE,
        required = true
    )
    val id: Int,

    @JsonProperty("firstName")
    @ApiModelProperty(
        value = FIRST_NAME_HELP,
        example = FIRST_NAME_EXAMPLE,
        required = true
    )
    val firstName: String,

    @JsonProperty("lastName")
    @ApiModelProperty(
        value = LAST_NAME_HELP,
        example = LAST_NAME_EXAMPLE,
        required = true
    )
    val lastName: String,

    @JsonProperty("email")
    @ApiModelProperty(
        value = EMAIL_HELP,
        example = EMAIL_EXAMPLE,
        required = true
    )
    val email: String,

    @JsonProperty("createdTimestamp")
    @ApiModelProperty(
        value = CREATION_TIMESTAMP_HELP,
        example = CREATION_TIMESTAMP_EXAMPLE,
        dataType = "String",
        required = true
    )
    val creationTimestamp: Instant,

    @JsonProperty("birthDate")
    @ApiModelProperty(
        value = BIRTH_DATE_HELP,
        example = BIRTH_DATE_EXAMPLE,
        required = true
    )
    val birthDate: LocalDate,

    @JsonProperty("deletedTimestamp")
    @ApiModelProperty(
        value = CREATION_TIMESTAMP_HELP,
        example = CREATION_TIMESTAMP_EXAMPLE,
    )
    val deletedTimestamp: Instant?
)

data class UserCreation(
    @get:Size(min = 1, max = 100)
    @JsonProperty("firstName")
    @ApiModelProperty(
        value = FIRST_NAME_HELP,
        example = FIRST_NAME_EXAMPLE,
        required = true
    )
    val firstName: String,

    @get:Size(min = 1, max = 100)
    @JsonProperty("lastName")
    @ApiModelProperty(
        value = LAST_NAME_HELP,
        example = LAST_NAME_EXAMPLE,
        required = true
    )
    val lastName: String,

    @get:Email
    @get:Size(min = 1, max = 100)
    @JsonProperty("email")
    @ApiModelProperty(
        value = EMAIL_HELP,
        example = EMAIL_EXAMPLE,
        required = true
    )
    val email: String,

    @JsonProperty("birthDate")
    @ApiModelProperty(
        value = BIRTH_DATE_HELP,
        example = BIRTH_DATE_EXAMPLE,
        dataType = "String",
        required = true
    )
    val birthDate: LocalDate
)