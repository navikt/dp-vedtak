plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20240704.83.dca2f1")
        }
    }
}

rootProject.name = "dp-behandling"

include("dato")
include("modell")
include("openapi")
include("opplysninger")
include("dagpenger")
include("mediator")
include("avklaring")
include("konklusjon")
include("dag")
