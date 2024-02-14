plugins {
    id("common")
    `java-library`
}

dependencies {
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))

    implementation("com.github.navikt:dp-grunnbelop:2023.05.24-15.26.f42064d9fdc8")
}
