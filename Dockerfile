FROM ghcr.io/navikt/baseimages/temurin:19

COPY mediator/build/libs/*.jar app.jar
