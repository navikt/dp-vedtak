plugins {
    id("common")
    `java-library`
}

dependencies {
    api("com.fasterxml.uuid:java-uuid-generator:4.3.0")
    testImplementation(libs.kotest.assertions.core)
}
