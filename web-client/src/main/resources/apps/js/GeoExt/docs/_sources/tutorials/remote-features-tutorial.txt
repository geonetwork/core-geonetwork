.. highlight:: javascript
   :linenothreshold: 4

====================
Vector Data Tutorial
====================

Base layers such as OpenStreetMap and Google Maps are generally distributed in
pre-rendered tiles using file formats such as PNG or JPG. While these are great
for **displaying** maps, they are not very useful for getting at the data behind
a map. They don't allow you to provide functionality such as informational
popups, selection and highlighting of individual features, and editing of data.
For these, you need to use **vector data**, provided through file formats such
as KML, GeoJSON, or GML which provide information about each feature on the map,
rather than just the pixels to put on the screen.

.. note:: Web browsers impose a same origin policy on JavaScript code to protect
    users from cross-site scripting attacks. This means that if your GeoExt
    application is hosted on a different host or port from your vector data, you
    will need to configure a proxy service.

Reading KML
===========

As an introduction to using vector data in GeoExt, let's create a simple map
that displays data from a KML. Copy :download:`this sample KML file
<sundials.kml>` to the same directory with your GeoExt and Ext libraries. Then
we can load it with some JavaScript::

    var map = new Openlayers.Map();
    var bluemarble = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {layers: "bluemarble"}
    );
    var sundials = new OpenLayers.Layer.Vector("Sundials");
    map.addLayer(bluemarble);
    map.addLayer(sundials);

    var store = new GeoExt.data.FeatureStore({
        layer: sundials,
        proxy: new GeoExt.data.ProtocolProxy({
            protocol: new OpenLayers.Protocol.HTTP({
                url: "sundials.kml",
                format: new OpenLayers.Format.KML()
            })
        }),
        fields: [
            {name: 'title', type: 'string'},
            {name: 'description', type: 'string'}
        ],
        autoLoad: true
    });

    var mapPanel = new GeoExt.MapPanel({
        title: "Sundials",
        map: map,
        renderTo: 'mapPanel',
        height: 400,
        width: 600
    });

Here, we set up a map with two layers. ``bluemarble`` is a WMS layer, which you
should have seen before in other tutorials. ``sundials`` is a vector layer,
which handles client-side rendering of vector data.

In **line 10** we initialize a :class:`GeoExt.data.FeatureStore`\ . This class
functions as a normal ``Ext.data.Store`` to interoperate with ExtJS classes, as
well as providing the ability to **bind** to an ``OpenLayers.Layer.Vector`` in
order to display features on a map. In this example, we set up the store
completely through constructor parameters:
    
    ``layer: sundials``
        tells the store to render features using the ``sundials`` layer. This is
        equivalent to calling ``store.bind(sundials)`` after initializing the
        store.

    ``proxy: new GeoExt.data.ProtocolProxy(``
        tells the store to use a ``ProtocolProxy`` for fetching features.
        ``ProtocolProxy`` wraps OpenLayers Protocol objects. Here we use an
        ``OpenLayers.Protocol.HTTP`` to fetch data over the web. The ``HTTP``
        protocol works with a variety of ``OpenLayers.Format`` types; here we
        use ``KML`` to match our dataset. You can see all the available
        ``Protocol``\ s and ``Format``\ s in the `OpenLayers API documentation
        <http://openlayers.org>`_.

    ``fields: [...]``
        tells the store which extra properties (aside from just the geometry) to
        look for. Here, we know that KML includes a ``title`` and a
        ``description`` for each point, and that both are string values.

    ``autoLoad: true``
        tells the store to go ahead and fetch the feature data as soon as the
        constructor finishes. This is equivalent to calling ``store.load()``
        after the store is initialized.

Now we have a map with a background and some data hosted on our server. It looks
like any other map; we can pan and zoom normally to navigate around.

However, since GeoExt has access to the data *behind* the map, we now have some
options that weren't available to us before. For example, we can add a control
that allows us to view the features in a tabular format::
    
    new Ext.grid.GridPanel({
        title: 'Sundials',
        store: store
        columns: [{heading: 'Title', dataIndex: 'title'},
                  {heading: 'Description', dataIndex: 'description'}],
        renderTo: "grid",
        width: 200,
        height: 600
    });

