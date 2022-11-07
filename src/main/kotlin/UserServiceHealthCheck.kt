import com.codahale.metrics.health.HealthCheck

class UserServiceHealthCheck(private val version: String) : HealthCheck() {
    @Throws(Exception::class)
    override fun check(): Result {
        return if (UserResource.getCount() == 0) {
            Result.unhealthy(
                "No persons in DB! Version: " +
                        version
            )
        } else Result.healthy(
            "OK with version: " + version +
                    ". Persons count: " + UserResource.getCount()
        )
    }
}