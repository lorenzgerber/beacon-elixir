# Dockerfile mostly based on egacrg/beacon
FROM debian:latest
LABEL maintainer = "Lorenz Gerber <gerberlo@gis.a-star.ed.sg>"
RUN apt-get clean -y; \
    apt-get update -y -qq; \
    apt-get install -y --no-install-recommends -q wget netcat postgresql-client default-jre-headless default-jdk-headless

RUN apt-get -y install maven git  || \
    apt-get -y update && apt-get -y install maven git
WORKDIR /tmp
RUN git clone https://github.com/lorenzgerber/beacon-elixir.git human-data-beacon 
RUN sed -i 's#allowed\.origins=.*$#allowed.origins=*#' /tmp/human-data-beacon/elixir_beacon/src/main/resources/META-INF/corsFilter.properties
RUN sed -i 's#^\s*datasource.elixirbeacon.url=.*$#datasource.elixirbeacon.url=jdbc\\:postgresql\\://beacon_db\\:5432/elixir_beacon_dev#g' \
    /tmp/human-data-beacon/elixir_beacon/src/main/resources/application-dev.properties
RUN grep datasource.elixirbeacon.url /tmp/human-data-beacon/elixir_beacon/src/main/resources/application-dev.properties

WORKDIR /tmp/human-data-beacon/elixir_core
RUN mvn clean compile jar:jar  
RUN mvn install -DskipTests

WORKDIR /tmp/human-data-beacon/elixir_beacon
RUN mvn clean compile package -Dspring.profiles.active="dev" -Dmaven.test.skip=true 

RUN cp /tmp/human-data-beacon/elixir_beacon/target/elixir-beacon-0.3.jar /tmp/elixirbeacon-service.jar
RUN rm -rf /tmp/human-data-beacon/
RUN apt-get -y remove git maven wget && apt-get -y autoremove

COPY beacon.sh /tmp
EXPOSE 9075
CMD /bin/sh /tmp/beacon.sh

# ##########################
# ## Build env
# ##########################
# FROM maven:3.6-jdk-8-alpine as BUILD

# RUN mkdir /beacon

# COPY elixir_core /beacon/elixir_core

# WORKDIR /beacon/elixir_core
# RUN mvn clean compile jar:jar -Djar.finalName=elixir-core
# RUN mvn install:install-file -Dfile=target/elixir-core.jar

# COPY elixir_beacon /beacon/elixir_beacon

# WORKDIR /beacon/elixir_beacon
# RUN echo 'allowed.origins=*' > src/main/resources/META-INF/corsFilter.properties
# RUN mvn clean compile package -Dspring.profiles.active="dev" -Dmaven.test.skip=true


# ##########################
# ## Final image
# ##########################
# FROM openjdk:8-jdk-alpine

# LABEL maintainer "CRG Developers"
# LABEL org.label-schema.schema-version=${DOCKER_TAG}
# LABEL org.label-schema.build-date=$BUILD_DATE
# LABEL org.label-schema.vcs-url="https://github.com/ga4gh-beacon/beacon-elixir"
# LABEL org.label-schema.vcs-ref=$SOURCE_COMMIT

# RUN mkdir -p /beacon

# COPY --from=BUILD /beacon/elixir_beacon/target/elixir-beacon.jar /beacon/beacon.jar
# COPY --from=BUILD /beacon/elixir_beacon//src/main/resources/application-dev.properties /beacon/application-dev.properties

# EXPOSE 9075
# WORKDIR /beacon

# CMD ["java", "-jar", "beacon.jar", "--spring.profiles.active=dev"]

