# Advanced configuration

## User session timeout configuration {#session-timeout-configuration}

Default session timeout is set to 35 minutes (See [User session](../administrator-guide/managing-users-and-groups/index.md#user-session)). This timeout can be configured in `WEB-INF/web.xml` by changing the value of the session-timeout (time is in minute):

``` xml
<session-config>
    <session-timeout>35</session-timeout>
</session-config>
```

For developers, the timeout is defined as a build property and can be customized in filters. See `web/src/main/filters/dev.properties#L20`
