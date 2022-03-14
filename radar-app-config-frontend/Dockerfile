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

# Stage 1, "build-stage", based on Node.js, to build and compile Angular
FROM --platform=$BUILDPLATFORM node:16 as builder

WORKDIR /code

COPY package*.json /code/

RUN npm install

COPY .browserslistrc angular.json karma.conf.js ts* /code/
COPY src /code/src

ARG configuration=production

RUN npm run build -- --output-path=./dist/out --configuration ${configuration}

WORKDIR /code/dist/out

# Stage 2, based on Nginx, to have only the compiled app, ready for production with Nginx
FROM nginxinc/nginx-unprivileged:1.20-alpine

ENV BASE_HREF=/appconfig/
ENV AUTH_URL=http://localhost:8080/managementportal/oauth
ENV AUTH_CALLBACK_URL=http://localhost:8080/appconfig/login
ENV APP_CONFIG_URL=/appconfig/api

# add init script
COPY docker/optimization.conf /etc/nginx/conf.d/
COPY --chown=101 docker/default.conf /etc/nginx/conf.d/
COPY docker/30-env-subst.sh /docker-entrypoint.d/

COPY --from=builder /code/dist/out/ /usr/share/nginx/html
COPY --from=builder --chown=101 /code/dist/out/main-* /code/dist/out/index.html* /usr/share/nginx/html/

