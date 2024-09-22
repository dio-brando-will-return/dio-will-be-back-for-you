plugins {
    kotlin("jvm") version "2.0.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.ktor:ktor-html-builder:1.6.8")
    implementation("io.ktor:ktor-server-sessions:2.3.9")
    implementation("io.ktor:ktor-server-auth:2.3.9")
    implementation("io.ktor:ktor-server-mustache:2.3.9")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

application {
    mainClass.set("main.kotlin.MainKt")
}

