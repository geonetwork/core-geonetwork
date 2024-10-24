# Change default view configuration {#configdefaultview}

## The Settings

In order to enable developers to specify some configuration settings, we tried as much as possible to use settings instead of hardcoded value in core code.

Those settings are mainly stored in 2 AngularJS `value` objects:

-   `gnViewerSettings`
-   `gnSearchSettings`

Those objects are injected via AngularJs `$inject` concept into all core services and directives.

## Viewer settings

The `gnViewSettings` object contains all settings regarding to the map viewer page.

-   `defaultContext` : path to the default OWS Context. If not set, the `ol.View` object needs to be defined for your map.
-   `bgLayers` : Array of layers that will be managed by the background layer selector. This value is used only if `defaultContext` is not defined.
-   `servicesUrl` : Config object for predefined WMS and WMTS servers entries.

## Search settings

The `gnSearchSettings` object contains all settings regarding to the main search page.

-   `olStyles` : `ol.style.Style` definition for some features in the map (drawn object, measure, metadata extent bounding box etc\...).
-   `viewerMap` : The `ol.Map` object used for the viewer. This map object may be extended if you have set the `defaultContext` setting.
-   `searchMap` : The `ol.Map` object used for the search page.
-   `hitsperpageValues` : An array to specify the possible values for the number of metadata by result page.
-   `sortbyValues` : An array to specify the possible values for the sort by option in the result list page.
-   `resultViewTpls` : The template used to render search result. By default it is a grid.
-   `formatter` : Set options to the metadata view.
-   `linkTypes` : Binding between resources protocol and action in metadata result list (for example open WMS in mapviewer).

## Update this settings

Note that some settings are set in the database (for example the `viewerSettings.mapConfig` object comes from database settings) so you can edit them in [settings admin page](../../administrator-guide/configuring-the-catalog/system-configuration.md).

For others, you can just edit the config file (See `web-ui/src/main/resources/catalog/views/default/config.js`) and change some setting values.

!!! info "Important"

    If you change existing files, you expose yourself to having merge issues on new version upgrades. The best way keeps to create your own view.

