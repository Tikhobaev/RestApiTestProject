package management

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton


val managementModule = Kodein.Module("management") {
    bind<Jdbi>() with singleton { Jdbi.create(instance<TestProjectConfiguration>().getDataSourceFactory()!!.url).apply {
        installPlugin(SqlObjectPlugin())
        installPlugin(KotlinPlugin())
        installPlugin(KotlinSqlObjectPlugin())
    }}
    bind<UserDAO>() with singleton { instance<Jdbi>().onDemand(UserDAO::class.java) }
    bind<UserResource>() with singleton { UserResource(instance()) }
    bind<UserServiceHealthCheck>() with singleton { UserServiceHealthCheck(instance<TestProjectConfiguration>().version) }
}