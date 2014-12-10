(function() {
  goog.provide('gn_ows_service');


  var module = angular.module('gn_ows_service', [
  ]);

  module.provider('gnOwsCapabilities', function() {
    this.$get = ['$http', 'gnUrlUtils', 'gnGlobalSettings', '$q',
      function($http, gnUrlUtils, gnGlobalSettings, $q) {

        var displayFileContent = function(data) {
          var layers = [];
          var layerSelected = null; // the layer selected on user click
          var layerHovered = null; // the layer when mouse is over it

          var parser = new ol.format.WMSCapabilities();
          var result = parser.read(data);

          var layers = [];
          var url = result.Capability.Request.GetCapabilities.
              DCPType[0].HTTP.Get.OnlineResource;

          // Push all leaves into a flat array of Layers.
          var getFlatLayers = function(layer) {
            if (angular.isArray(layer)) {
              for (var i = 0, len = layer.length; i < len; i++) {
                getFlatLayers(layer[i]);
              }
            } else if (angular.isDefined(layer)) {
              layer.url = url;
              layers.push(layer);
              getFlatLayers(layer.Layer);
            }
          };

          // Make sur Layer property is an array even if
          // there is only one element.
          var setLayerAsArray = function(node) {
            if (node) {
              if (angular.isDefined(node.Layer) &&
                  !angular.isArray(node.Layer)) {
                node.Layer = [node.Layer];
              }
              if (angular.isDefined(node.Layer)) {
                for (var i = 0; i < node.Layer.length; i++) {
                  setLayerAsArray(node.Layer[i]);
                }
              }
            }
          };
          getFlatLayers(result.Capability.Layer);
          setLayerAsArray(result.Capability);
          result.Capability.layers = layers;
          return result.Capability;
        };

        var parseWMTSCapabilities = function(data) {
          var parser = new ol.format.WMTSCapabilities();
          var result = parser.read(data);

          result.contents.Layer = result.contents.layers;
          result.contents.operationsMetadata = result.operationsMetadata;
          return result.contents;
        };

        var mergeDefaultParams = function(url, defaultParams) {
          //merge URL parameters with default ones
          var parts = url.split('?');
          var urlParams = angular.isDefined(parts[1]) ?
              gnUrlUtils.parseKeyValue(parts[1]) : {};

          for (var p in urlParams) {
            defaultParams[p] = urlParams[p];
            if (defaultParams.hasOwnProperty(p.toLowerCase()) &&
                p != p.toLowerCase()) {
              delete defaultParams[p.toLowerCase()];
            }
          }

          return gnUrlUtils.append(parts[0],
              gnUrlUtils.toKeyValue(defaultParams));


        };
        return {

          getWMSCapabilities: function(url) {
            var defer = $q.defer();
            if (url) {
              url = mergeDefaultParams(url, {
                service: 'WMS',
                request: 'GetCapabilities'
              });

              //send request and decode result
              if (gnUrlUtils.isValid(url)) {
                var proxyUrl = gnGlobalSettings.proxyUrl +
                    encodeURIComponent(url);
                $http.get(proxyUrl, {
                  cache: true
                })
                  .success(function(data, status, headers, config) {
                      defer.resolve(displayFileContent(data));
                    })
                  .error(function(data, status, headers, config) {
                      defer.reject(status);
                    });
              }
            }
            return defer.promise;
          },

          getWMTSCapabilities: function(url) {
            var defer = $q.defer();
            if (url) {
              url = mergeDefaultParams(url, {
                REQUEST: 'GetCapabilities'
              });

              if (gnUrlUtils.isValid(url)) {

                var proxyUrl = gnGlobalSettings.proxyUrl +
                    encodeURIComponent(url);
                $http.get(proxyUrl, {
                  cache: true
                })
                    .success(function(data, status, headers, config) {
                      defer.resolve(parseWMTSCapabilities(data));
                    })
                    .error(function(data, status, headers, config) {
                      defer.reject(status);
                    });
              }
            }
            return defer.promise;
          },


          getLayerExtentFromGetCap: function(map, getCapLayer) {
            var extent = null;
            var layer = getCapLayer;
            var srsCode = map.getView().getProjection().getCode();

            //var ext = layer.BoundingBox[0].extent;
            //var olExtent = [ext[1],ext[0],ext[3],ext[2]];
            // TODO fix using layer.BoundingBox[0].extent
            // when sextant fix his capabilities
            if (angular.isArray(layer.BoundingBox)) {
              extent = ol.proj.transform(layer.EX_GeographicBoundingBox,
                  //layer.BoundingBox[0].crs,
                  'EPSG:4326',
                  srsCode);
            }
            return extent;
          },

          getLayerInfoFromCap: function(name, capObj) {
            for (var i = 0, len = capObj.layers.length;
                 i < len; i++) {
              if (name == capObj.layers[i].Name ||
                  name == capObj.layers[i].identifier) {
                return capObj.layers[i];
              }
            }
          }
        };
      }];
  });
})();
