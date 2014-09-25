(function() {
  goog.provide('gn_owscontext_service');

  var module = angular.module('gn_owscontext_service', []);

  // OWC Client
  // Jsonix wrapper to read or write OWS Context
  var context =  new Jsonix.Context(
    [XLink_1_0, OWS_1_0_0, Filter_1_0_0, GML_2_1_2, SLD_1_0_0, OWC_0_3_1],
    {
      namespacePrefixes : {
        "http://www.w3.org/1999/xlink": "xlink",
        "http://www.opengis.net/ows": "ows"
      }
    }
  );
  var unmarshaller = context.createUnmarshaller();
  var marshaller = context.createMarshaller();

  module.service('gnOwsContextService', [
    '$http',
    function($http) {

      /**
       * Loads a context, ie. creates layers and centers the map
       * @param context object
       */
      this.loadContext = function(text, map) {
        var context = unmarshaller.unmarshalString(text).value;
        // first remove any existing layer
        map.getLayers().forEach(function(layer) {
            console.info('Layer removed: ', layer);
            map.removeLayer(layer);
        });

        // set the General.BoundingBox
        var bbox = context.general.boundingBox.value;
        var ll = bbox.lowerCorner;
        var ur = bbox.upperCorner;
        var extent = ll.concat(ur);
        var projection = bbox.crs;
        // reproject in case bbox's projection doesn't match map's projection
        extent = ol.proj.transformExtent(extent, map.getView().getProjection(), projection);
        map.getView().fitExtent(extent, map.getSize());

        // load the resources
        var layers = context.resourceList.layer;
        var i, olLayer;
        for (i = 0; i < layers.length; i++) {
            var layer = layers[i];
            if (layer.name.indexOf('google') != -1) {
                // pass
            } else if (layer.name.indexOf('osm') != -1) {
                var osmSource = new ol.source.OSM();
                olLayer = new ol.layer.Tile({source: osmSource});
            } else {
                var server = layer.server[0];
                if (server.service == 'urn:ogc:serviceType:WMS') {
                    var onlineResource = server.onlineResource[0];
                    var source = new ol.source.ImageWMS({
                        url: onlineResource.href,
                        params: {'LAYERS': layer.name}
                    });
                    olLayer = new ol.layer.Image({ source: source });
                }
            }
            if (olLayer) {
                olLayer.setOpacity(layer.opacity);
                olLayer.setVisible(!layer.hidden);
                olLayer.set('group', layer.group);
                map.addLayer(olLayer);
            }
        }
      };

      /**
       * Loads a context from an URL.
       * @param url URL to context
       * @param map map
       */
      this.loadContextFromUrl = function(url, map) {
        var self = this;
        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
        $http.get(proxyUrl).success(function(data) {
          self.loadContext(data, map);
        });
      };

      /**
       * Creates a javascript object based on map context then marshals it
       *    into XML
       * @param context object
       */
      this.writeContext = function(map) {

        var extent = map.getView().calculateExtent(map.getSize());

        var general = {
          boundingBox: {
            name: {
              "namespaceURI": "http://www.opengis.net/ows",
              "localPart": "BoundingBox"
            },
            value: {
              crs: map.getView().getProjection().getCode(),
              lowerCorner: [extent[0], extent[1]],
              upperCorner: [extent[2], extent[3]]
            }
          }
        };

        var resourceList = {
          layer: []
        };
        map.getLayers().forEach(function(layer) {
          var source = layer.getSource();
          var url = "";
          var name;
          if (source instanceof ol.source.OSM) {
            name = "{type=osm}";
          } else if (source instanceof ol.source.ImageWMS) {
            name = source.getParams().LAYERS;
            url = layer.getSource().getUrl();
          }
          resourceList.layer.push({
            hidden: layer.getVisible(),
            opacity: layer.getOpacity(),
            name: name,
            title: layer.get('title'),
            group: layer.get("group"),
            server: [{
              onlineResource: [{
                href: url
              }],
              service: "urn:ogc:serviceType:WMS"
            }]
          });
        });

        var context = {
          version: "0.3.1",
          id: "ows-context-ex-1-v3",
          general: general,
          resourceList: resourceList
        };

        var xml = marshaller.marshalDocument({
          name: {
            localPart: 'OWSContext',
            namespaceURI: "http://www.opengis.net/ows-context",
            prefix: "ows-context",
            string: "{http://www.opengis.net/ows-context}ows-context:OWSContext"
          },
          value: context
        });
        return xml;
      }
    }
  ]);
})();
