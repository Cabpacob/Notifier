import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor", "ktor-client-core", "2.2.1")
    implementation("io.ktor", "ktor-client-cio", "2.2.1")
    implementation("org.jsoup", "jsoup", "1.15.3")
    implementation("commons-io", "commons-io", "2.11.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.27.0")
    implementation("org.seleniumhq.selenium:selenium-devtools-v128:4.26.0")

    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "notifier.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
