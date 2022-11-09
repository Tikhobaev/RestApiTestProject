package management

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

private const val ID_HELP = "User unique identifier"
private const val ID_EXAMPLE = "1"

private const val FIRST_NAME_HELP = "User first name"
private const val FIRST_NAME_EXAMPLE = "Joe"

private const val LAST_NAME_HELP = "User last name"
private const val LAST_NAME_EXAMPLE = "Biden"

private const val EMAIL_HELP = "User email. Must be unique"
private const val EMAIL_EXAMPLE = "joe.biden@gmail.com"

data class User(
    @JsonProperty("id")
    @ApiModelProperty(
        value = ID_HELP,
        example = ID_EXAMPLE,
        required = true
    )
    val id: Int,

    @Min(1)
    @Max(100)
    @JsonProperty("firstName")
    @ApiModelProperty(
        value = FIRST_NAME_HELP,
        example = FIRST_NAME_EXAMPLE,
        required = true
    )
    val firstName: String,

    @Min(1)
    @Max(100)
    @JsonProperty("lastName")
    @ApiModelProperty(
        value = LAST_NAME_HELP,
        example = LAST_NAME_EXAMPLE,
        required = true
    )
    val lastName: String,

    @Email
    @Min(1)
    @Max(100)
    @JsonProperty("email")
    @ApiModelProperty(
        value = EMAIL_HELP,
        example = EMAIL_EXAMPLE,
        required = true
    )
    val email: String,
    // Birth date, creation timestamp, deleted timestamps (Instant)
    )

data class UserCreation(
    @JsonProperty("firstName")
    @Min(1)
    @Max(100)
    val firstName: String,

    @JsonProperty("lastName")
    val lastName: String,

    @JsonProperty("email")
    @Email
    val email: String,
    // Birth date
)


class _User {
    @XmlElement(name = "id")
    @get:JsonProperty
    var id = 0
        private set

    @XmlElement(name = "firstName")
    @get:JsonProperty
    var firstName: String? = null
        private set

    @XmlElement(name = "lastName")
    @get:JsonProperty
    var lastName: String? = null
        private set

    @XmlElement(name = "email")
    @get:JsonProperty
    var email: String? = null
        private set


    constructor() {}

    constructor(id: Int, firstName: String?, lastName: String?, email: String?) {
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }
}