# Advanced configuration

## User session timeout configuration {#session-timeout-configuration}

Default session timeout is set to 35 minutes (See [User session](../administrator-guide/managing-users-and-groups/index.md#user-session)). This timeout can be configured in `WEB-INF/web.xml` by changing the value of the session-timeout (time is in minute):

``` xml
<session-config>
    <session-timeout>35</session-timeout>
</session-config>
```

For developers, the timeout is defined as a build property and can be customized in filters. See `web/src/main/filters/dev.properties#L20`

## Saxon tree model configuration
By default, Saxon uses the TinyTree model to represent XML documents in memory. This is a compact and efficient representation suitable for most applications.
In the case of GN, it can be measured (running edit.xsl transformation), that linked or tinyc tree models are faster than tiny tree model.
So Saxon is configured to use tinyc if available, and linked if tinyc not available.

One can choose to use tiny tree model by setting SAXON_TREE_MODEL system property to 1 at startup.


