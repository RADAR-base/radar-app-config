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
FROM node:14 as node
WORKDIR /app
COPY package*.json /app/
RUN npm install
COPY . /app/
ARG configuration=production
RUN npm run build -- --output-path=./dist/out --configuration $configuration

# Stage 2, based on Nginx, to have only the compiled app, ready for production with Nginx
FROM nginx:1.19.2-alpine

## Remove default Nginx website
#RUN rm -rf /usr/share/nginx/html/*

COPY ./docker/nginx.conf /etc/nginx/nginx.conf
COPY --from=node /app/dist/out/ /usr/share/nginx/html

COPY ./docker/entrypoint.sh /entrypoint.sh

# expose internal port:80 and run init.sh
EXPOSE 80

ENTRYPOINT ["sh", "/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
