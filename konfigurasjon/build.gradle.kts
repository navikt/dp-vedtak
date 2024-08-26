plugins {
    id("common")
    `java-library`
}

dependencies {
    implementation(libs.konfig)
    implementation("io.getunleash:unleash-client-java:9.2.0")
}
