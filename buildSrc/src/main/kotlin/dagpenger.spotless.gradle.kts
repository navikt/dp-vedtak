import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(Ktlint.version)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("spotlessApply")
}
