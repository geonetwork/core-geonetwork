/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: example[renderer]
 *  Feature Renderer
 *  ----------------
 *  Render a vector feature with multiple symbolizers in a box component.
 */

var blue = {
    fillColor: "blue",
    fillOpacity: 0.25,
    strokeColor: "blue",
    strokeWidth: 2,
    pointRadius: 5
};

var custom = {
    point: {
        graphicName: "star",
        pointRadius: 8,
        fillColor: "yellow",
        strokeColor: "red",
        strokeWidth: 1
    },
    line: {
        strokeColor: "#669900",
        strokeWidth: 3
    },
    poly: {
        fillColor: "olive",
        fillOpacity: 0.25,
        strokeColor: "#666666",
        strokeWidth: 2,
        strokeDashstyle: "dot"
    }
};

var stacked = {
    point: [{
        pointRadius: 8,
        fillColor: "white",
        strokeColor: "red",
        strokeWidth: 2
    }, {
        graphicName: "star",
        pointRadius: 5,
        fillColor: "red"
    }],
    line: [{
        strokeColor: "red",
        strokeWidth: 5
    }, {
        strokeColor: "#ff9933",
        strokeWidth: 2
    }],
    poly: [{
        strokeWidth: 3,
        fillColor: "white",
        strokeColor: "#669900"
    }, {
        strokeWidth: 2,
        fillOpacity: 0,
        strokeColor: "red",
        strokeDashstyle: "dot"
    }]
};

var configs = [{
    symbolType: "Point",
    renderTo: "point_default"
}, {
    symbolType: "Line",
    renderTo: "line_default"
}, {
    symbolType: "Polygon",
    renderTo: "poly_default"
}, {
    symbolType: "Point",
    symbolizers: [blue],
    renderTo: "point_blue"
}, {
    symbolType: "Line",
    symbolizers: [blue],
    renderTo: "line_blue"
}, {
    symbolType: "Polygon",
    symbolizers: [blue],
    renderTo: "poly_blue"
}, {
    symbolType: "Point",
    symbolizers: [custom.point],
    renderTo: "point_custom"
}, {
    symbolType: "Line",
    symbolizers: [custom.line],
    renderTo: "line_custom"
}, {
    symbolType: "Polygon",
    symbolizers: [custom.poly],
    renderTo: "poly_custom"
}, {
    symbolType: "Point",
    symbolizers: stacked.point,
    renderTo: "point_stacked"
}, {
    symbolType: "Line",
    symbolizers: stacked.line,
    renderTo: "line_stacked"
}, {
    symbolType: "Polygon",
    symbolizers: stacked.poly,
    renderTo: "poly_stacked"
}];

Ext.onReady(function() {        
    for(var i=0; i<configs.length; ++i) {
        new GeoExt.FeatureRenderer(configs[i]);
    }
    $("render").onclick = render;
});

var format = new OpenLayers.Format.WKT();
var renderer, win;
function render() {
    var wkt = $("wkt").value;
    var feature;
    try {
        feature = format.read(wkt)
    } catch(err) {
        $("wkt").value = "Bad WKT: " + err;
    }
    var symbolizers;
    try {
        var value = $("symbolizers").value;
        symbolizers = eval("(" + value + ")");
        if (!symbolizers || symbolizers.constructor !== Array) {
            throw "Must be an array literal";
        }
    } catch(err) {
        $("symbolizers").value = "Bad symbolizers: " + err + "\n\n" + value;
        symbolizers = null;
    }
    if(feature && symbolizers) {
        if(!win) {
            renderer = new GeoExt.FeatureRenderer({
                feature: feature,
                symbolizers: symbolizers,
                width: 150,
                style: {margin: 4}
            });
            win = new Ext.Window({
                closeAction: "hide",
                layout: "fit",
                width: 175,
                items: [renderer]
            });
        } else {
            renderer.update({
                feature: feature,
                symbolizers: symbolizers
            });
        }
        win.show();
    }
}

