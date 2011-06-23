===================
 GeoExt QuickStart
===================

Welcome to GeoExt!  This document is intended to help you get started
with GeoExt.  With GeoExt, you can start from nothing and have a rich
mapping application in seconds.


Getting GeoExt
==============

GeoExt is built on top of the robust OpenLayers JavaScript mapping
library and the rich graphical components of ExtJS.  For licensing
reasons, ExtJS cannot be included in the GeoExt download, so preparing
GeoExt for use on your own web pages is a multi-step process:

#.  Download GeoExt from the :doc:`downloads page </downloads>`. For the purposes
    of this quickstart, the development version will be fine.

#.  Download OpenLayers 2.10 or later from http://openlayers.org/. 

#.  Download ExtJS 3.2 from `the ExtJS website <http://www.sencha.com/products/js/download.php>`_.

#.  Place both unpacked libraries in a directory that is published by your web
    server. For this tutorial, I will assume that this is the root of your web
    server, so that GeoExt.js is at http://localhost/GeoExt/lib/GeoExt.js and
    ext-all.js is at http://localhost/ext-3.2.1/ext-all.js

#.  Now you're ready to use GeoExt in your application!

.. note:: For production environments, the GeoExt team recommends that
    you use compressed and minified builds of GeoExt and ExtJS to
    optimize the download size of your page.  A generic minified build
    containing all of GeoExt is available from the
    :doc:`downloads page </downloads>`, but advanced users can build their
    own.



Basic Example
=============

Let's build a simple web page that just embeds a map with interactive
navigation.

#.  Include the ExtJS libraries in your web page.

    .. code-block:: html
    
        <script src="ext-3.2.1/adapter/ext/ext-base.js" type="text/javascript"></script>
        <script src="ext-3.2.1/ext-all.js"  type="text/javascript"></script>
        <link rel="stylesheet" type="text/css" href="ext-3.2.1/resources/ext-all.css"></link>
        <script src="OpenLayers/OpenLayers.js" type="text/javascript"></script>
        <script src="GeoExt/lib/GeoExt.js" type="text/javascript"></script>
        <link rel="stylesheet" type="text/css" href="GeoExt/resources/geoext-all-debug.css"></link>

#.  Create a ``<div>`` element in your web page with its ``id``
    attribute set to ``gxmap``.  We will use the ``id`` to attach a
    GeoExt component to the ``div``.

#.  Attach a ``MapPanel`` object to the ``div`` with some JavaScript code:

    .. code-block:: html 
    
        <script type="text/javascript">
            Ext.onReady(function() {
                var map = new OpenLayers.Map();
                var layer = new OpenLayers.Layer.WMS(
                    "Global Imagery",
                    "http://maps.opengeo.org/geowebcache/service/wms",
                    {layers: "bluemarble"}
                );
                map.addLayer(layer);
    
                new GeoExt.MapPanel({
                    renderTo: 'gxmap',
                    height: 400,
                    width: 600,
                    map: map,
                    title: 'A Simple GeoExt Map'
                });
            });
        </script>

The entire source of your page should look something like:

.. code-block:: html

    <html>
    <head>

    <title> A Basic GeoExt Page </title>
    <script src="ext-3.2.1/adapter/ext/ext-base.js" type="text/javascript"></script>
    <script src="ext-3.2.1/ext-all.js"  type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="ext-3.2.1/resources/ext-all.css"></link>
    <script src="OpenLayers/OpenLayers.js" type="text/javascript"></script>
    <script src="GeoExt/lib/GeoExt.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="GeoExt/resources/geoext-all-debug.css"></link>

    <script type="text/javascript">
        Ext.onReady(function() {
            var map = new OpenLayers.Map();
            var layer = new OpenLayers.Layer.WMS(
                "Global Imagery",
                "http://maps.opengeo.org/geowebcache/service/wms",
                {layers: "bluemarble"}
            );
            map.addLayer(layer);

            new GeoExt.MapPanel({
                renderTo: 'gxmap',
                height: 400,
                width: 600,
                map: map,
                title: 'A Simple GeoExt Map'
            });
        });
    </script>
    </head>
    <body>
    <div id="gxmap"></div>
    </body>
    </html>

And that's it! You now have all of GeoExt, ready to bring your geospatial data
to life. Go forth and prosper!

Going Further
=============

From here, there are a wide variety of options available for making
customized, highly interactive mapping applications with GeoExt.  To
learn more take a look at :doc:`index`, :ref:`examples <examples>` and
:doc:`/lib/index`.

We also recommend reading :doc:`../primers/ext-primer` and
:doc:`../primers/openlayers-primer` to become acquainted with the libraries that
form the foundation of GeoExt.

