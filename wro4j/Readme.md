# Module Purpose
[Web Resources Optimizer for Java (WRO4J)](https://code.google.com/p/wro4j/wiki/Introduction) is used by Geonetwork for Javascript
and CSS minification.  This module has some customization to the out-of-the-box implementation.  A few customizations are:

* Custom Wro Model Factory
  * The factory uses a custom XML model file called `wro-sources.xml` which is based on the `wro-sources.xsd` schema.  The documentation for
   writing the XML file is in the XSD file.
  * This custom factory allows the Javascript files and dependencies to be declared in the file using `goog.provide` and `goog.require`
    declarations (see https://developers.google.com/closure/library/docs/introduction).
    In WRO4J lingo, all `requireJsSources` Javascript files found (see `requireJsSourceType` in `wro-sources.xsd`) are parsed for `goog.provide`
    and `goog.require` files. The dependencies of each Javascript file will be included in the WRO4J group.
    All dependencies and the Javascript file will be minified together as one group and the dependency order will be maintained.
  * The factory will create a `closure_deps.js` file which is needed by closure `base.js` for determining which javascript files to load.  This
    is used when in debug mode.
  * The model factory also allows an explicit ordering of Javascript files when that is required.
  * Finally, the model factory allows `wro-sources.xml` files to be included in a root `wro-sources.xml` file.  This allows the `wro-sources.xml`
    to be kept in the module it applies to and just be included from the `web-app` module.
* A Less Compiler that can handle both css and `less` files.
* A Processor that removes `goog.provide` and `goog.require` (because they are not needed in the minified javascript because all dependencies
  are included)
* Other support classes for Wro Model Factory are included in this module (like `ClosureDependencyUriLocator`)

# Wro4j Configuration Files

* __`wro-sources.xml`__ - Declares the javascript and css groups.  The `wro-sources.xsd` contains information on how to write such a file.
  At the time of this writing there is a root `wro-sources.xml` file in `web/src/main/webResources/WEB-INF/wro-sources.xml` which includes:
   * `web-ui/src/main/resources/web-ui-wro-sources.xml`
* __`wro.properties`__ - The wro configuration. See https://code.google.com/p/wro4j/wiki/ConfigurationOptions for the list and
  description of properties in the file.
* __`web/pom.xml`__ - This file contains `build/plugins/plugin` plugin for `wro4j` which runs the minification and some minor checks on the `wro4j`
  configuration during a build.  The intention is to catch any major configuration errors at build time.
