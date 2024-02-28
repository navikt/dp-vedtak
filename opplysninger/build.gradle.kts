plugins {
    id("common")
    `java-library`
}

dependencies {
    api("com.fasterxml.uuid:java-uuid-generator:4.3.0")
    implementation("no.bekk.bekkopen:nocommons:0.16.0")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.kotest.assertions.core)
}
