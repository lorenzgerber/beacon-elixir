##########################
## Build env
##########################
FROM maven:3.6-jdk-8-alpine as BUILD

RUN mkdir /beacon

COPY elixir_core /beacon/elixir_core

WORKDIR /beacon/elixir_core
RUN mvn clean compile jar:jar -Djar.finalName=elixir-core
RUN mvn install:install-file -Dfile=target/elixir-core.jar

COPY elixir_beacon /beacon/elixir_beacon

WORKDIR /beacon/elixir_beacon
RUN echo 'allowed.origins=*' > src/main/resources/META-INF/corsFilter.properties
RUN mvn clean compile package -Dspring.profiles.active="dev" -Dmaven.test.skip=true


##########################
## Final image
##########################
FROM openjdk:8-jdk-alpine

LABEL maintainer "CRG Developers"
LABEL org.label-schema.schema-version=${DOCKER_TAG}
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.vcs-url="https://github.com/ga4gh-beacon/beacon-elixir"
LABEL org.label-schema.vcs-ref=$SOURCE_COMMIT

RUN mkdir -p /beacon

COPY --from=BUILD /beacon/elixir_beacon/target/elixir-beacon.jar /beacon/beacon.jar
COPY --from=BUILD /beacon/elixir_beacon//src/main/resources/application-dev.properties /beacon/application-dev.properties

EXPOSE 9075
WORKDIR /beacon

CMD ["java", "-jar", "beacon.jar", "--spring.profiles.active=dev"]
