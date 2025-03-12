# Wro4j resource management {#wro4jdependencies}

Wro4j lib is used to manage resources of the web applications. Mostly, it

-   builds dependency tree between js source files
-   minifies javascript
-   compiles less files
-   manages live update in development mode
-   manages caching in production mode

## Dependency Tree

When the AngularJs entry module is `gn_search_geoportal`, the wro4j entry point is the key `gn_search_geoportal` as well, that means that it is the file that declares `goog.provide('gn_search_geoportal')`.

That is why you need to be sure there is a good mapping between AngularJs module dependencies and wro4j dependency tree.

For example, when you have this for wro4j:

``` js
goog.provide('gn_search_default');

goog.require('gn_search');
goog.require('gn_search_default_config');
goog.require('gn_related_directive');
```

You need to be sure you create your AngularJs module with same dependency injection:

``` js
var module = angular.module('gn_search_default',
    [
      'gn_search',
      'gn_search_default_config',
      'gn_related_directive'
    ]);
```

!!! info "Important"

    Whenever you change a `goog.require` or `goog.provide` statements, you need to relaunch wro4j dependency tree creation. This can be done with Jetty or Tomcat restart.


Following this principles, you are now free to add new modules, components, directives or services to your application.
