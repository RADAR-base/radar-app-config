# RADAR app config

<!-- TOC -->
* [RADAR app config](#radar-app-config)
  * [Supported API](#supported-api)
  * [Docker usage](#docker-usage)
  * [Sentry monitoring](#sentry-monitoring)
<!-- TOC -->

Aimed to be an app configuration engine with per-project and per-conditional configuration. This will possibly be merged with [RADAR AppServer](https://github.com/radar-base/radar-appserver) in the future. 

Intended features:

- Create default app config, with OAuth clients taken from ManagementPortal
- Create per-project app config, with projects taken from ManagementPortal
- Create per-project conditional configuration, using a simple expression language
- Upload user statistics from app.
- Get custom client-user configuration.

For example, default config would be
```yaml
clientA:
  sendRate: 50

clientB:
  sendRate: 100
```

Project A would have config
```yaml
clientB:
  plugins: A B
  collectRate: 15
```

With conditional
```yaml
id: 1
name: SpecificUserAbcd
expression: user.id == 'abcd1234...'
config:
  clientB:
    plugins: B
```

Then user `abcd1234...` using `clientB` would get the following config: 

```yaml
plugins: B
sendRate: 100
collectRate: 15
```

## Supported API

The current iteration of this project has a subset of the intended functionality implemented. Each variable is resolved based on the scope of a variable. The scopes are ordered, with the first taking least precedence and the last taking higher precedence. By settings a variable explicitly to `null` in a given scope, this unsets the variable in a parent scope. The scope ordering is as follows:

1. `global`
2. `project.{projectId}`
3. `group.{groupId}` (not implemented yet)
4. `condition.{conditionId}` (not implemented yet)
5. `user.{userId}`

Settings in the global scope can be seen as the platform defaults. They can only be updated by the system administrator. Project and user configs can only be updated by project admins.

List supported clients. This list can be configured in ManagementPortal
```
GET /clients
---
HTTP 200 OK
{
  "clients": [
    {"clientId": "clientA"}
  ]
}
```

List projects that the current user is project admin for.
```
GET /projects
---
HTTP 200 OK
{
  "projects": [
    {"id": 1, projectName: "projectA", humanReadableProjectName: "Project A", "location": "Utrecht, The Netherlands", "organization": "The Hyve", "description": "My project plan."}
  ]
}
```

Get the default config (global scope) for a client. Sys admin only.
```
GET /global/config/{clientId}
---
HTTP 200 OK
{
  "clientId": "{clientId}"
  "scope": "global",
  "config": [
    {"name": "plugins", "value": "A", "scope": "global"}
  ]
}
```

Replace the default config (global scope) for a client. Sys admin only.
```
POST /global/config/{clientId}
{
  "config": [
    {"name": "plugins", "value": "A"}
  ]
}
---
HTTP 200 OK
{
  "clientId": "{clientId}"
  "scope": "global",
  "config": [
    {"name": "{name}", "value": "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Get the most recent value for a specific config name in the global scope.
```
GET /global/config/{clientId}/names/{name}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "global",
  "config": [
    {"name": "{name}", "value": "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ],
  "defaults": []
}
```

List all versions for a specific config name in the global scope.
```
GET /global/config/{clientId}/names/{name}/versions
---
HTTP 200 OK

{
"clientId": "{clientId}",
"scope": "global",
"config": [
  {"name": "{name}", "value": "B", "scope": "global", "clientId": "{clientId}", "version": 2, "createdByUser": "appconfig", "createTimestamp": 1764068409792},
  {"name": "{name}", "value": "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
]
}

```

Get one specific version of a config name in the global scope.
```
GET /global/config/{clientId}/names/{name}/versions/{version}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "global",
  "config": [
    {"name": "{name}", "value": "A", "scope": "global", "clientId": "{clientId}", "version": {version}, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Get the per-project config.
```
GET /projects/{projectId}/config/{clientId}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "project.projectA",
  "config": [
    {"name": "plugins", "value": "A B", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ],
  "defaults": [
    {"name": "plugins", "value", "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Update the per-project config.
```
POST /projects/{projectId}/config/{clientId}
{
  "config": [
    {"name": "rate", "value": "1"}
    {"name": "plugins", "value": "A B"}
  ]
}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "project.projectA",
  "config": [
    {"name": "plugins", "value": "A B", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791},
    {"name": "rate", "value": "1", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ],
  "defaults": [
    {"name": "plugins", "value", "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Get the most recent value for a specific config name in the project scope.
```
GET /projects/{projectId}/config/{clientId}/names/{name}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "project.{projectId}",
  "config": [
    {"name": "{name}", "value": "A", "scope": "project.{projectId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

List all versions for a specific config name in the project scope.
```
GET /projects/{projectId}/config/{clientId}/names/{name}/versions
---
HTTP 200 OK

{
"clientId": "{clientId}",
"scope": "project.{projectId}",
"config": [
    {"name": "{name}", "value": "A","scope": "project.{projectId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791},
    {"name": "{name}", "value": "B","scope": "project.{projectId}", "clientId": "{clientId}", "version": 2, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
]
}

```

Get one specific version of a config name in the project scope.
```
GET /projects/{projectId}/config/{clientId}/names/{name}/versions/{version}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "project.{projectId}",
  "config": [
    {"name": "{name}", "value": "A B", "version": {version}, "scope": "project.{projectId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Get the per-user config. This is the call that end-user clients should make.
```
GET /projects/{projectId}/users/{userId}/config/{clientId}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "user.sub-1",
  "config": [
    {"name": "plugins1", "value": "D", "scope": "user.sub-1", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ],
  "defaults": [
    {"name": "plugins1", "value": "C", "scope": "project.radar", "clientId": "{clientId}", "version": 2, "createdByUser": "appconfig", "createTimestamp": 1764079198971},
    {"name": "plugins2", "value": "A", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]
}
```

Update the per-user config.
```
POST /projects/{projectId}/users/{userId}/config/{clientId}
{
  "config": [
    {"name": "rate", "value": "1"}
  ]
}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "user.userA",
  "config": [
    {"name": "rate", "value": "1"}
  ],
  "defaults": [
    {"name": "plugins", "value": "A B", "scope": "project.projectA"},
    {"name": "rate", "value": "1", "scope": "project.projectA"}
  ]
}
```

Get the most recent value for a specific config name for a user.
```
GET /projects/{projectId}/users/{userId}/config/{clientId}/names/{name}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "user.{userId}",
  "config": [
    {"name": "{name}", "value": "1", "scope": "user.{userId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}
  ]

}
```

List all user-scope versions for a specific config name for a user.
```
GET /projects/{projectId}/users/{userId}/config/{clientId}/names/{name}/versions
---
HTTP 200 OK

{
"clientId": "{clientId}",
"scope": "user.{userId}",
"config": [
    {"name": "{name}", "value": "2", "scope": "user.{userId}", "clientId": "{clientId}", "version": 2, "createdByUser": "appconfig", "createTimestamp": 1764068409791}}
    {"name": "{name}", "value": "1", "scope": "user.{userId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}}
]
}

```

Get one specific user-scope version of a config name for a user.
```
GET /projects/{projectId}/users/{userId}/config/{clientId}/names/{name}/versions/{version}
---
HTTP 200 OK
{
  "clientId": "{clientId}",
  "scope": "user.{userId}",
  "config": [
    {"name": "{name}", "value": "1", "scope": "user.{userId}", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}}
  ]
}
```

Get the public configuration. This returns the global configuration for the special public client.
```
GET /public/config
---
HTTP 200 OK
{
  "clientId": "public_config_service",
  "scope": "global",
  "config": [
    {"name": "somePublicSetting", "value": "on", "scope": "global", "clientId": "{clientId}", "version": 1, "createdByUser": "appconfig", "createTimestamp": 1764068409791}}
  ]
}

```

## Docker usage

Start the stack with
```
docker-compose up -d
```

and once you're finished, stop it with

```
docker-compose down
```

Then you can test requests with Postman at root URL `http://localhost:8080/appconfig/api/`. Start a Postman query. Add OAuth2 authorization and press the _Get new access token_ button. Use
```
Callback URL: http://localhost:8080/appconfig/login
Auth URL: http://localhost:8080/managementportal/oauth/authorize
Access Token URL: http://localhost:8080/managementportal/oauth/token
Client ID: appconfig_frontend
```
Leave `Client Secret`, `Scope` and `State` empty. Log in with user `admin`, password `admin` and accept the request. Once this is accepted, scroll down to indicate _Use token_. Now you can make any requests to the radar-app-config API.

To have any projects or subjects, these should be created by going to <http://localhost:8080/managementportal/> and logging in again with `admin`, `admin`.


## Sentry monitoring

To enable Sentry monitoring:

1. Set a `SENTRY_DSN` environment variable that points to the desired Sentry DSN.
2. (Optional) Set the `SENTRY_LOG_LEVEL` environment variable to control the minimum log level of events sent to Sentry.
   The default log level for Sentry is `ERROR`. Possible values are `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR`.

For further configuration of Sentry via environmental variables see [here](https://docs.sentry.io/platforms/java/configuration/#configuration-via-the-runtime-environment). For instance:

```
SENTRY_LOG_LEVEL: 'ERROR'
SENTRY_DSN: 'https://000000000000.ingest.de.sentry.io/000000000000'
SENTRY_ATTACHSTACKTRACE: true
SENTRY_STACKTRACE_APP_PACKAGES: io.confluent.connect,org.radarbase.connect.rest
```
