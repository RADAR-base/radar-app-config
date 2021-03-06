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

FROM gradle:7.0-jdk11 as builder

RUN mkdir /code
WORKDIR /code
ENV GRADLE_USER_HOME=/code/.gradlecache

COPY build.gradle.kts settings.gradle.kts gradle.properties /code/
COPY radar-app-config/build.gradle.kts  /code/radar-app-config/
COPY radar-expression-lang/build.gradle.kts  /code/radar-expression-lang/
RUN gradle :radar-app-config:downloadDependencies --no-watch-fs

COPY radar-expression-lang/src /code/radar-expression-lang/src
COPY radar-app-config/src /code/radar-app-config/src

RUN gradle :radar-app-config:distTar --no-watch-fs \
    && cd radar-app-config/build/distributions \
    && tar zxf *.tar.gz \
    && rm *.tar.gz radar-app-config*/lib/radar-app-config*.jar

FROM openjdk:11-jre-slim

MAINTAINER @nivemaham @blootsvoets

LABEL description="RADAR-base radar app config container"

# Override JAVA_OPTS to set heap parameters, for example
ENV JAVA_OPTS="" \
    RADAR_APP_CONFIG_OPTS=""

RUN mkdir -p /var/lib/radar-app-config/data
WORKDIR /var/lib/radar-app-config

RUN apt-get update && apt-get install -y \
  curl \
  && rm -rf /var/lib/apt/lists/*

COPY --from=builder /code/radar-app-config/build/distributions/radar-app-config*/bin/* /usr/bin/
COPY --from=builder /code/radar-app-config/build/distributions/radar-app-config*/lib/* /usr/lib/
COPY --from=builder /code/radar-app-config/build/libs/radar-app-config*.jar /usr/lib/

USER 101

EXPOSE 8090

CMD ["radar-app-config"]
