plugins {
    id("java")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

group = "hsmw.apmcreator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}