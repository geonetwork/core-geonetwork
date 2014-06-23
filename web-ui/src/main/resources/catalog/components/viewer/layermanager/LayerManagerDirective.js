(function () {
  goog.provide('gn_layermanager_directive');

  var module = angular.module('gn_layermanager_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnLayermanager', [
    'gnLayerFilters',
    function (gnLayerFilters) {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/viewer/layermanager/' +
        'partials/layermanager.html',
      scope: {
        map: '=gnLayermanagerMap'
      },
      link: function (scope, element, attrs) {

        scope.layerFilterFn = gnLayerFilters.selected;

        scope.removeLayerFromMap = function(layer) {
          scope.map.removeLayer(layer);
        };

        scope.moveLayer = function(layer, delta) {
          var index = scope.filterLayers.indexOf(layer);
          var layersCollection = scope.map.getLayers();
          layersCollection.removeAt(index);
          layersCollection.insertAt(index + delta, layer);
        };
      }
    };
  }]);

})();
