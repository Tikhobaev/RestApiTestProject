package management

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import javax.validation.constraints.NotEmpty


class TestProjectConfiguration : Configuration() {
    @get:JsonProperty
    @set:JsonProperty
    var version: @NotEmpty String = "1.0"

    @JsonProperty("swagger")
    var swaggerBundleConfiguration: SwaggerBundleConfiguration? = null
//    @Valid
//    @NotNull
//    private var database: DataSourceFactory = DataSourceFactory()
//
//    @JsonProperty("database")
//    fun setDataSourceFactory(factory: DataSourceFactory) {
//        database = factory
//    }
//
//    @JsonProperty("database")
//    fun getDataSourceFactory(): DataSourceFactory {
//        return database
//    }

}