============================
Layer Tree Tutorial
============================

Often when presenting users with an interactive map, it is useful to allow them
to control the visible layers. In this tutorial, we examine the use of
:class:`GeoExt.tree.LayerContainer` with the stock ``Ext.tree.TreePanel`` class
to accommodate toggling visibility of layers and rearranging their drawing
order.

.. note:: Before starting this tutorial, you should have a working
    :class:`GeoExt.MapPanel` in your page. The :doc:`mappanel-tutorial` will
    help you set one up if you don't already have one.

Start With a Map
================

Let's assume you already have a :class:`GeoExt.MapPanel` on your page with some
layers. In the :doc:`mappanel-tutorial`\ , we discussed how you can use the
``layers`` property of the ``MapPanel`` to add, remove, and modify the layers of
the map as well as monitor the layer list for changes. This is more than
sufficient to display a 'live' list of layers in an ``Ext.grid.GridPanel``\ .
The :class:`GeoExt.tree.LayerContainer` is another component that can listen to
changes to the map's layer list. However, rather than an independent panel, the
``LayerContainer`` is a node that must be contained in an ``Ext.tree.TreePanel``
to be displayed. Here's an example rendering a layer tree to a ``div``:

.. code-block:: javascript
       
    var mapPanel = new GeoExt.MapPanel({
        /* Your configuration here */
    });

    var layerList = new GeoExt.tree.LayerContainer({
        text: 'All Layers',
        layerStore: mapPanel.layers,
        leaf: false, 
        expanded: true
    });

    var layerTree = new Ext.tree.TreePanel({
        title: 'Map Layers',
        renderTo: 'layerTree',
        root: layerList
    });

``LayerContainer``\ s automatically add checkboxes (radio buttons for base
layers) that can be used to toggle the visibility of layers. You can also enable
drag-n-drop layer reordering by simply setting the ``enableDD`` property of the
``TreePanel``.

Filtering
=========
By default, the ``LayerContainer``'s ``LayerLoader`` automatically pulls in all layers from the store and displays those with the ``displayInLayerSwitcher``
property set to true. You can provide your own filter function to the loader:

.. code-block:: javascript

    var layerList = new GeoExt.tree.LayerContainer({
        text: 'Tasmania Layers',
        layerStore: mapPanel.layers,
        leaf: false, 
        expanded: true,
        loader: {
            filter: function(record) {
                return record.get("layer").name.indexOf("Tasmania") !== -1
            }
        }
    });

The above will only load layers with "Tasmania" in their name. By adding
multiple named and filtered ``LayerContainer``\ s to a ``TreePanel`` you are
able to provide logical organization to your layer trees. When ``enableDD`` is
set to true on the tree, drag-n-drop will also work between filtered layer
containers, as long as they have the same parent node. You can also directly
instantiate :class:`GeoExt.tree.LayerNode` to create tree nodes that can be
added anywhere in a tree. Keep in mind, however, that this approach does not
allow for automatic drag-n-drop support.

.. note::

    There are two LayerContainer types with a preconfigured filter:
    
    * :class:`GeoExt.tree.BaseLayerContainer` will be populated only with layers
      that have isBaseLayer set to true,
    * :class:`GeoExt.tree.OverlayLayerContainer` will be populated only with
      layers that have  isBaseLayer set to false.

Visibility Grouping
===================

The concept of a base layer in OpenLayers is just a gruop of layers that are on
the bottom of the layer stack, and only one can be visible at a time. In maps
without base layers (when ``allOverlays`` is set to true, the latter can be
enforced by configuring a ``checkedGroup`` on a LayerNode. Such a layer node
will be rendered with a radio button instead of a check box. Of all layers
configured with the same ``checkedGroup``, only one will be visible at a time:

.. code-block:: javascript

    var layerList = new GeoExt.tree.LayerContainer({
        text: 'Tasmania Layers',
        layerStore: mapPanel.layers,
        leaf: false, 
        expanded: true,
        loader: {
            filter: function(record) {
                return record.get("layer").name.indexOf("Tasmania") !== -1
            },
            baseAttrs: {
                checkedGroup: "tasmania"
            }
        }
    });

Layer Nodes with Additional Radio Buttons
=========================================

It is possible to render layer nodes with an additional radio button. This can
be useful if an application uses the concept of an "active layer". The active
layer can then be set by clicking its radio button:

.. code-block:: javascript

    var layerList = new GeoExt.tree.LayerContainer({
        text: 'All Layers',
        layerStore: mapPanel.layers,
        leaf: false, 
        expanded: true,
        loader: {
            baseAttrs: {
                radioGroup: "active"
            }
        }
    });
    var registerRadio = function(node)
        if(!node.hasListener("radiochange")) {
            node.on("radiochange", function(node){
                /* set your active layer here */
            });
        }
    }
    var layerTree = new Ext.tree.TreePanel({
        title: 'Map Layers',
        renderTo: 'layerTree',
        root: layerList,
        listeners: {
            append: registerRadio,
            insert: registerRadio
        }
    });

The layer node fires the "radiochange" event when the radio button is clicked.
The above snippet configures a listener for this event when a node is added to
or inserted in the tree.

Sub-Layers
==========

Layers that have a ``params`` property (like ``OpenLayers.Layer.WMS``) can be
used to create sub-layers based on one of the ``params`` properties. This is
useful to e.g. create sub-nodes from the layer object's "LAYERS" or "CQL_FILTER"
param:

.. code-block:: javascript

    var groupLayer = new OpenLayers.Layer.WMS("Tasmania (Group Layer)",
        "http://demo.opengeo.org/geoserver/wms", {
            layers: [
                "topp:tasmania_state_boundaries",
                "topp:tasmania_water_bodies",
                "topp:tasmania_cities",
                "topp:tasmania_roads"
            ],
            transparent: true,
            format: "image/gif"
        }
    );
    var groupLayerNode = new GeoExt.tree.LayerNode({
        layer: groupLayer,
        leaf: false, 
        expanded: true,
        loader: {
            param: "LAYERS"
        }
    });
    
.. note::
    The :class:`GeoExt.tree.LayerParamLoader` does not add drag-n-drop support
    to the sub-nodes it creates, so ``allowDrag`` and ``allowDrag`` should be
    set to false for a :class:`GeoExt.tree.LayerNode` configured with a
    :class:`GeoExt.class.LayerParamLoader`, unless you provide custom "move"
    handlers.

.. seealso:: The ExtJS TreePanel `documentation
    <http://dev.sencha.com/deploy/dev/docs/?class=Ext.tree.TreePanel>`_ and `examples
    <http://dev.sencha.com/deploy/dev/examples/#sample-7>`_ for more
    information about customizing tree panels.
