FROM cgr.dev/chainguard/jre:latest

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

COPY build/libs/mediator-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]