import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktlint("0.48.2")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.48.2")
    }
    // https://github.com/diffplug/spotless/issues/1644
    lineEndings = LineEnding.PLATFORM_NATIVE // or any other except GIT_ATTRIBUTES
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("spotlessApply")
}
