# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM gradle:7.3-jdk17 as builder

RUN mkdir /code
WORKDIR /code
ENV GRADLE_USER_HOME=/code/.gradlecache \
   GRADLE_OPTS=-Djdk.lang.Process.launchMechanism=vfork

COPY build.gradle.kts settings.gradle.kts gradle.properties /code/
COPY radar-app-config/build.gradle.kts  /code/radar-app-config/
COPY radar-expression-lang/build.gradle.kts  /code/radar-expression-lang/
COPY radar-expression-lang-antlr/build.gradle.kts  /code/radar-expression-lang-antlr/
COPY radar-app-config-core/build.gradle.kts  /code/radar-app-config-core/
COPY radar-app-config-client/build.gradle.kts  /code/radar-app-config-client/

RUN gradle downloadDependencies copyDependencies startScripts --no-watch-fs

COPY radar-expression-lang-antlr/src  /code/radar-expression-lang-antlr/src
COPY radar-app-config-core/src  /code/radar-app-config-core/src
COPY radar-expression-lang/src /code/radar-expression-lang/src
COPY radar-app-config/src /code/radar-app-config/src

RUN gradle :radar-app-config:jar --no-watch-fs

FROM azul/zulu-openjdk-alpine:17.0.2-jre-headless

MAINTAINER @nivemaham @blootsvoets

LABEL description="RADAR-base radar app config container"

# Override JAVA_OPTS to set heap parameters, for example
ENV JAVA_OPTS="" \
    RADAR_APP_CONFIG_OPTS=""

RUN apk add --no-cache curl
RUN mkdir -p /var/lib/radar-app-config/data
WORKDIR /var/lib/radar-app-config

COPY --from=builder /code/radar-app-config/build/third-party/* /usr/lib/
COPY --from=builder /code/radar-app-config/build/scripts/* /usr/bin/
COPY --from=builder /code/*/build/libs/* /usr/lib/

USER 101

EXPOSE 8090

CMD ["radar-app-config"]
