(function () {
  goog.provide('gn_wmsimport_directive');

  var module = angular.module('gn_wmsimport_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnWmsImport', [
    'gnOwsCapabilities',
    'gnMap',
    '$translate',
    function (gnOwsCapabilities, gnMap, $translate) {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/viewer/wmsimport/' +
        'partials/wmsimport.html',
      scope: {
        map: '=gnWmsImportMap'
      },
      link: function (scope, element, attrs) {
        scope.gnOwsCapabilities = gnOwsCapabilities;
        scope.selection = [];

        scope.params = {};

        //TODO: remove
        scope.servicesList = [
          'http://ids.pigma.org/geoserver/wms',
          'http://ids.pigma.org/geoserver/ign/wms',
          'http://www.ifremer.fr/services/wms/oceanographie_physique'
        ];
        scope.load = function (url) {
          scope.searchFilter = '';
          scope.selection = [];
          gnOwsCapabilities.getCapabilities(url)
            .then(function (layers) {
              scope.layers = layers;
            });
        };

        scope.select = function (layer) {
          if (scope.selection.indexOf(layer) < 0) {
            scope.selection.push(layer);
          }
          else {
            scope.selection.splice(scope.selection.indexOf(layer), 1);
          }
        };

        scope.addLayer = function(getCapLayer, isPreview) {

          if (getCapLayer) {

            try {
              var layer = getCapLayer;

              return gnMap.addWmsToMap(scope.map,
                {
                  LAYERS: layer.name
                },
                {
                  url: scope.params.url,
                  label: layer.title,
                  extent: gnOwsCapabilities.getLayerExtentFromGetCap(scope.map, layer),
/*
                  attribution: gaUrlUtils.getHostname($scope.fileUrl),
*/
                  preview: isPreview
                }
              );

            } catch (e) {
              scope.userMessage = $translate('add_wms_layer_failed') +
                e.message;
              return null;
            }
          }
        };

        scope.loadLayers = function(layers) {
          angular.forEach(layers, function(layer) {
            scope.addLayer(layer);
          });
        };
      }
    };
  }]);

})();
