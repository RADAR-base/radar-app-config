FROM gradle:6.6-jdk11 as builder

RUN mkdir /code
WORKDIR /code

COPY ./build.gradle.kts ./settings.gradle.kts /code/
COPY radar-app-config/build.gradle.kts  /code/radar-app-config/
COPY radar-expression-lang/build.gradle.kts  /code/radar-expression-lang/
RUN gradle :radar-app-config:downloadDependencies

COPY radar-expression-lang/src /code/radar-expression-lang/src
COPY radar-app-config/src /code/radar-app-config/src

RUN gradle -Dkotlin.compiler.execution.strategy="in-process" -Dorg.gradle.parallel=false -Pkotlin.incremental=false :radar-app-config:distTar \
    && cd radar-app-config/build/distributions \
    && tar xf *.tar \
    && rm *.tar radar-app-config*/lib/radar-app-config*.jar

FROM openjdk:11-jre-slim

MAINTAINER @nivemaham @blootsvoets

LABEL description="RADAR-base radar app config container"

# Override JAVA_OPTS to set heap parameters, for example
ENV JAVA_OPTS="" \
    RADAR_APP_CONFIG_OPTS="-Djava.security.egd=/dev/./urandom"

RUN mkdir -p /var/lib/radar-app-config/data
WORKDIR /var/lib/radar-app-config

COPY --from=builder /code/radar-app-config/build/distributions/radar-app-config*/bin/* /usr/bin/
COPY --from=builder /code/radar-app-config/build/distributions/radar-app-config*/lib/* /usr/lib/
COPY --from=builder /code/radar-app-config/build/libs/radar-app-config*.jar /usr/lib/

EXPOSE 8090

CMD ["radar-app-config"]
