
plugins {
    id("common")
    `java-library`
}
dependencies {
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))
    implementation(project(path = ":uuid-v7"))

    testImplementation(libs.kotest.assertions.core)
}
