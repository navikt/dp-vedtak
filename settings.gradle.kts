rootProject.name = "dp-vedtak"
include("modell")
include("mediator")
include("openapi")


dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20230928.37.cc12c9")
        }
    }
}
