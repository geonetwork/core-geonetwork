# Monitoring

GeoNetwork publishes health check and metrics endpoints intended for monitoring tools such as
[Zabbix](https://www.zabbix.com/), [Nagios](https://www.nagios.org/) or a container orchestrator
liveness probe.

## Health check endpoints

Health checks are split into three groups, each with its own endpoint. The paths below assume
the application is deployed with the default `geonetwork` context path:

| Endpoint | Group | Suggested use |
|----------|-------|---------------|
| `/geonetwork/criticalhealthcheck` | Checks that must pass for the catalogue to be functional | Poll frequently, alert on failure |
| `/geonetwork/warninghealthcheck` | Checks reporting a degraded catalogue that still functions | Poll frequently, warn on failure |
| `/geonetwork/expensivehealthcheck` | Checks that must pass but are expensive to compute | Poll infrequently |

The checks registered in each group are configured in `WEB-INF/config/config-service-monitoring.xml`:

| Group | Checks |
|-------|--------|
| Critical | `DatabaseHealthCheck`, `CswGetCapabilitiesHealthCheck`, `IndexHealthCheck` |
| Warning | `DeadlockedThreadsHealthCheck`, `HarvestersHealthCheck`, `DashboardAppHealthCheck`, `NoIndexErrorsHealthCheck`, `IndexReadOnlyHealthCheck`, `FreeConnectionsHealthCheck`, `FreeFileHandlesHealthCheck` |
| Expensive | `CswGetRecordsHealthCheck` |

Access requires authentication as a user with the `Monitor` profile. `Administrator` accounts also
hold this authority.

## Health check response

The response status code reports the group as a whole:

-   `200 OK`: every check in the group reported `OK` or `DISABLED`.
-   `500 Internal Server Error`: at least one check in the group reported `ERROR`.

The body is a JSON array with one object per check:

``` json
[
  {
    "name": "DeadlockedThreadsHealthCheck",
    "status": "OK"
  },
  {
    "name": "DashboardAppHealthCheck",
    "status": "DISABLED",
    "msg": "Kibana is currently not enabled."
  },
  {
    "name": "HarvestersHealthCheck",
    "status": "ERROR",
    "msg": "Harvester failure",
    "exception": "org.example.SomeException: ..."
  }
]
```

| Field | Description |
|-------|-------------|
| `name` | Class name of the check, as listed above |
| `status` | Check outcome, see below |
| `msg` | Description of the outcome, absent when a successful check has nothing to report |
| `exception` | Stack trace, present only when a check failed with an exception |

`status` takes one of the following values:

| Status | Description |
|--------|-------------|
| `OK` | The check verified the component successfully |
| `DISABLED` | The component is optional and turned off by configuration, so it was not verified. This is not a failure and does not affect the response status code |
| `ERROR` | The check failed, see `msg` and `exception` |

!!! info "Version Added"

    4.4.13

    `DISABLED` was added in 4.4.13. Monitoring tools written against earlier
    versions only expect `OK` and `ERROR`, so alert on the response status code
    rather than on the absence of `OK`, and treat an unrecognised status as
    non-failing.

!!! note

    When a group has no checks registered, the response is `200 OK` with the body
    `["No health checks registered."]`, an array of strings rather than of objects.

## Dashboard health check

`DashboardAppHealthCheck` verifies the Kibana instance configured by the `kb.url` property.
The property is empty by default, so a catalogue that does not use dashboards reports
`DISABLED` rather than a failure, and the dashboard features are hidden in the admin console.

### Enabling the dashboards

As shipped, `kb.url` takes the value of the `GEONETWORK_KIBANA_URL` environment variable when
that variable is set, and the value built into the application otherwise. Setting the variable is
therefore the simplest way to point a deployed application, in particular a container, at a
Kibana instance:

``` bash
GEONETWORK_KIBANA_URL=http://kibana:5601
```

Alternatively, set a plain value in `WEB-INF/config.properties`. This replaces the expression, so
the environment variable is no longer read:

``` properties
kb.url=http://localhost:5601
```

When building from source, set the Maven property to build the URL into the application:

``` bash
mvn clean install \
    -Dkb.url=http://localhost:5601
```

`kb.url` drives the health check and the admin console. The catalogue also proxies the dashboards
at `/geonetwork/dashboards`, and that proxy resolves its target separately, from the
`geonetwork.HttpDashboardProxy.targetUri` property, falling back to the `kb.url` value that was
built into `WEB-INF/web.xml`. When enabling dashboards on an application that was built without a
URL, set the proxy target as well, either as an environment variable
(`GEONETWORK_HTTPDASHBOARDPROXY_TARGETURI`), a system property, or an entry in
`WEB-INF/config.properties`:

``` properties
geonetwork.HttpDashboardProxy.targetUri=http://localhost:5601
```

### Disabling the dashboards

Leave `kb.url` empty, which is the default. To disable the dashboards on an application that was
built with a URL, set the environment variable to an empty value:

``` bash
GEONETWORK_KIBANA_URL=
```

Or set the property to an empty value in `WEB-INF/config.properties`:

``` properties
kb.url=
```

!!! note

    Disabling the dashboards stops the health check from contacting Kibana and hides the
    dashboard features in the admin console. It does not affect the
    `/geonetwork/dashboards` proxy, whose target comes from
    `geonetwork.HttpDashboardProxy.targetUri` or, by default, from the value built into
    `WEB-INF/web.xml`.

## Metrics endpoints

The `/geonetwork/monitor` servlet exposes the underlying metrics registry and requires the same
`Monitor` authority:

| Endpoint | Description |
|----------|-------------|
| `/geonetwork/monitor/metrics` | All registered metrics as JSON. Accepts a `pretty` boolean and a `class` metric name to filter on |
| `/geonetwork/monitor/threads` | Text representation of a thread dump taken at the time of the call |
| `/geonetwork/monitor/healthcheck` | All health checks as plain text, returning `200` if all pass and `500` otherwise |

!!! note

    `/geonetwork/monitor/healthcheck` reports every check, including those of the three
    groups above, but it does not know about the `DISABLED` status: a disabled check is
    reported as `OK` with the internal message form, for example
    `DISABLED: Kibana is currently not enabled.`. Use the group endpoints to distinguish
    a disabled component from a verified one.

The health checks are also reported in the admin console under
``Admin --> Statistics & status``.
