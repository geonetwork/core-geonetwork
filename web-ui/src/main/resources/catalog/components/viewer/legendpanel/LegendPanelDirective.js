(function() {
  goog.provide('gn_legendpanel_directive');

  var module = angular.module('gn_legendpanel_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLegendPanel
   *
   * @description
   * This directive `gnLegendPanel` is a panel containing all legends of all
   * active layers in the map.
   */
  module.directive('gnLegendPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLegendPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'legendpanel.html',
        link: function(scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayerorderPanel
   *
   * @description
   * This directive `gnLayerorderPanel` is a panel which offers tools to change
   * active layers order in the map.
   */
  module.directive('gnLayerorderPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLayerorderPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'layerorderpanel.html',
        link: function(scope, element, attrs) {

          var map = scope.map;
          scope.layers = map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;

          /**
           * Change layer index in the map.
           *
           * @param {ol.layer} layer
           * @param {float} delta
           */
          scope.moveLayer = function(layer, delta) {
            var layersCollection = map.getLayers();
            var index = layersCollection.getArray().indexOf(layer);
            layersCollection.removeAt(index);
            layersCollection.insertAt(index + delta, layer);
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayersourcesPanel
   *
   * @description
   * This directive `gnLayersourcesPanel` is a panel that displays a list of
   * all layers and their sources.
   */
  module.directive('gnLayersourcesPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLayersourcesPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'layersources.html',
        link: function(scope, element, attrs) {

          var map = scope.map;
          scope.layers = map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;
        }
      };
    }]);

})();
