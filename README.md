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