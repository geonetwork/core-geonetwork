/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/data/AttributeReader.js
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = AttributeStore
 *  base_link = `Ext.data.DataStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.DataStore>`_
 */
Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: AttributeStore(config)
 *  
 *      Small helper class to make creating stores for remotely-loaded attributes
 *      data easier. AttributeStore is pre-configured with a built-in
 *      ``Ext.data.HttpProxy`` and :class:`gxp.data.AttributeReader`.  The
 *      HttpProxy is configured to allow caching (disableCaching: false) and
 *      uses GET. If you require some other proxy/reader combination then you'll
 *      have to configure this with your own proxy or create a basic
 *      ``Ext.data.Store`` and configure as needed.
 */

/** api: config[format]
 *  ``OpenLayers.Format``
 *  A parser for transforming the XHR response into an array of objects
 *  representing attributes.  Defaults to an
 *  ``OpenLayers.Format.WFSDescribeFeatureType`` parser.
 */

/** api: config[fields]
 *  ``Array or Function``
 *  Either an array of field definition objects as passed to
 *  ``Ext.data.Record.create``, or a record constructor created using
 *  ``Ext.data.Record.create``.  Defaults to ``["name", "type"]``. 
 */
GeoExt.data.AttributeStore = function(c) {
    c = c || {};
    GeoExt.data.AttributeStore.superclass.constructor.call(
        this,
        Ext.apply(c, {
            proxy: c.proxy || (!c.data ?
                new Ext.data.HttpProxy({url: c.url, disableCaching: false, method: "GET"}) :
                undefined
            ),
            reader: new GeoExt.data.AttributeReader(
                c, c.fields || ["name", "type"]
            )
        })
    );
};
Ext.extend(GeoExt.data.AttributeStore, Ext.data.Store);
