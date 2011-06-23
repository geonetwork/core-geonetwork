.. highlight:: javascript

============================
``MapPanel`` Tutorial
============================

The :class:`GeoExt.MapPanel` is the heart of most GeoExt applications,
displaying rendered data. Leveraging the OpenLayers JavaScript mapping library,
it can display rendered tiles from OWS services, perform client-side rendering
in the browser, and use tiles from popular mapping services such as Google Maps
or Virtual Earth. In this tutorial, we explore ways that developers can
customize the MapPanel.

.. note:: 
  It is recommended that you follow the :doc:`quickstart` tutorial
  before moving on to this one. It really is quick; we'll wait for you.

A Basic MapPanel
================

Taking a look at the example code from the quickstart tutorial, we can see a
very basic map configuration:
       
.. code-block:: javascript
    :linenos:

    var map = new OpenLayers.Map();
    var layer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );
    map.addLayer(layer);
     
    var mapPanel = new GeoExt.MapPanel({
        renderTo: 'gxmap',
        height: 400,
        width: 600,
        map: map,
        title: 'A Simple GeoExt Map'
    });

Looking at this code we can see a few things going on:

In **line 1** we instantiate an :class:`OpenLayers.Map`. This isn't required by
the MapPanel (it will create a Map for you if none is provided) but we want to
customize our map a bit.

In **lines 2-6** we create a new :class:`OpenLayers.Layer`. This particular
layer is a WMS layer, which uses tiles from the Blue Marble layer at
http://maps.opengeo.org/. 

In **line 7** we add our new layer to the map.

In **lines 9-15** we create a new map panel with several options:

    ``renderTo``
       This works the same as ``renderTo`` in a normal :class:`Ext.Panel`; it
       can be an id string, DOM node, or :class:`Ext.Element` telling the
       MapPanel where on the page it should insert itself.

    ``height``, ``width``
       These tell the map panel how much large it should draw itself.

    ``map``
       This is an :class:`OpenLayers.Map` which will be used as the actual map
       inside the panel. 

    ``title``
       This is the normal ``title`` property for ExtJS components. It will be
       rendered nicely across the top of the panel.

Working with the MapPanel
=========================
While using ``OpenLayers.Map.addLayer()`` to add layers is a convenient way to
customize the map, a hand-coded, static list of map layers is not always what we
want. In order to make manipulating the layer list more accessible to ExtJS
widgets, the MapPanel exposes a `layers` property which is an
:class:`Ext.data.Store` that will automatically be updated when layers are
added, removed, changed, or reordered, with all of the Ext events that go with
it. We can use this to, for example, populate an :class:`Ext.grid.GridPanel`
with a live list of layers in the map::
    
    new Ext.grid.GridPanel({
        renderTo: 'layerlist',
        height: 200, width: 200,
        autoScroll: true,
        store: mapPanel.layers,
        columns: [{name: 'name', heading: 'Name'}]
    });


In the HTML, you'll need to add a ``div`` for the grid panel to render itself in:

.. code-block:: html

    <div id='layerlist'></div>

More information on the :class:`Ext.grid.GridPanel` is available from the `ExtJS
API documentation
<http://dev.sencha.com/deploy/dev/docs/?class=Ext.grid.GridPanel>`_.

.. note:: 
  This code is only meant as an example to demonstrate the map panel's
  integration with Ext. An :class:`Ext.tree.TreePanel` with
  :class:`GeoExt.tree.LayerNode`\ s is a a much nicer way to display the layers in
  a map, with optional support for hiding/showing layers and reordering. The
  TreePanel approach is discussed in the :doc:`layertree-tutorial`.
