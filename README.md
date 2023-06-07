# dp-vedtak

Vedtakmodell for Dagpenger

## Komme i gang

### Hente github-package-registry pakker fra NAV-IT
Enkelte avhengigheter kan være lastet opp til Github Package Registry, som krever autentisering for å kunne lastes ned. Ved bruk av Gradle, kan man legge til følgene i `build.gradle.kts`:

```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        credentials {
            username = githubUser
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/REPO")
    }
}
```

`githubUser` og `githubPassword` er da properties som settes i `~/.gradle/gradle.properties`:

```                                                     
githubUser=x-access-token
githubPassword=<token>
```

Hvor `<token>` er et personal access token med scope `read:packages`.
Om du ikke har access token lages token med scope `read:packages` i github (inne under [developer settings](https://github.com/settings/tokens)). Husk å enable SSO for tokenet.

Alternativt kan variablene kan også konfigureres som miljøvariabler, eller brukes i kommandolinjen:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

```
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```

Gradle brukes som byggverktøy og er bundlet inn.

```
./gradlew build
```
# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-dagpenger-dev.