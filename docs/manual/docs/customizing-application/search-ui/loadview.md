# Create your own view {#loadsearchpage}

The search page is served by the ``catalog.search`` service.

This service ends up into `base-layout-cssjs-loader.xsl` (See `web/src/main/webapp/xslt/base-layout-cssjs-loader.xsl`) which contains the logic to load the right html page.

## The view parameter

You can pass a parameter `view` to the ``catalog.search`` url to point on a custom view.

If no parameter is specified, the view value will be taken from the `system/ui/defaultView` settings, and if no setting is set, is value will be `default`.

It will be stored in `$searchView` XSLT variable.

## Loading the view

The view is loaded from (See `web/src/main/webapp/xslt/ui-search/search.xsl`):

``` xml
<div data-ng-include="'{$uiResourcesPath}views/{$searchView}/templates/index.html'"
    class="gn-full">
```

The view main template is loaded through the AngularJS directive `ng-include` and will point on the path `/web-ui/src/main/resources/catalog/views/${view}/templates/index.html`.

For the default view, you can find this template in `web-ui/src/main/resources/catalog/views/default/templates/index.html`.

## Loading the javascript

To init AngularJS main module you have to use the directive ``ng-app`` and point on your main AngularJS module.

``` xml
<html ng-app="{$angularModule}" lang="{$lang}" id="ng-app">
```

In the case of the search page (``catalog.search``), the `$angularApp` variable equals `gn_search`. And for the `default` view the `$angularModule` equals `gn_search_default`.

So, by default, the main AngularJS module is loaded here from `gn_search_default` module declared here (See `web-ui/src/main/resources/catalog/views/default/module.js`.

From here, you can build your AngularJS module dependency tree and load your application.

## CSS files

In the same logic, the CSS that is loaded into the page is `gn_search_default.less`.
