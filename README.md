# RADAR app config

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
    {"name": "plugins", "value": "A"}
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
    {"name": "plugins", "value": "A B"}
  ],
  "defaults": [
    {"name": "plugins", "value", "A", "scope": "global"
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
    {"name": "plugins", "value": "A B"},
    {"name": "rate", "value": "1"}
  ],
  "defaults": [
    {"name": "plugins", "value", "A", "scope": "global"
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
  "scope": "user.userA",
  "config": [
    {"name": "plugins", "value": "A B"}
  ],
  "defaults": [
    {"name": "plugins", "value": "A B", "scope": "project.projectA"},
    {"name": "rate", "value": "1", "scope": "project.projectA"}
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