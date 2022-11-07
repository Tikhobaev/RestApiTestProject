import com.fasterxml.jackson.annotation.JsonProperty
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "User")
class User {
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