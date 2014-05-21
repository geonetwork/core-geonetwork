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
    'gnOwsCapabilities',
    'gaLayerFilters',
    '$translate',
    function (gnOwsCapabilities, gaLayerFilters, $translate) {
    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/layermanager/' +
        'partials/layermanager.html',
      scope: {
        map: '=gnLayermanagerMap'
      },
      link: function (scope, element, attrs) {

        scope.layers = scope.map.getLayers().getArray();

        scope.layerFilter = gaLayerFilters.selected;

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
