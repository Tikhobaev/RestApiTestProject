package management

import com.codahale.metrics.health.HealthCheck

class UserServiceHealthCheck(private val version: String) : HealthCheck() {
    @Throws(Exception::class)
    override fun check(): Result {
        return Result.healthy("Ok!")
    }
}