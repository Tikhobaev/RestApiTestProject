import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.userRegistryApp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("io.dropwizard:dropwizard-core:2.0.0")
    implementation("io.dropwizard:dropwizard-jdbi3:2.0.0")
    implementation("com.smoketurner:dropwizard-swagger:2.0.0-1")
    implementation("com.h2database:h2:1.3.148")
    implementation("commons-collections:commons-collections:3.2")
    implementation("org.jdbi:jdbi3-core:3.11.1")
    implementation("org.jdbi:jdbi3-kotlin:3.11.1")
    implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.11.1")
    implementation("com.github.salomonbrys.kodein:kodein:4.1.0")

    testImplementation("io.dropwizard:dropwizard-testing:2.0.0")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("management.UserRegistryApp")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            exclude("META-INF/*.DSA")
            exclude("META-INF/*.RSA")
            exclude("META-INF/*.SF")
            attributes(mapOf("Main-Class" to "management.UserRegistryApp"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}