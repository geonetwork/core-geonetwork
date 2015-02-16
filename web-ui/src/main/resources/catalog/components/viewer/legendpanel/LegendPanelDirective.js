(function() {
  goog.provide('gn_legendpanel_directive');

  var module = angular.module('gn_legendpanel_directive', [
  ]);

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

          var map = scope.map;

          map.getLayers().on('change:length', function(e) {

            var fLayers = $filter('filter')(map.getLayers().getArray(),
                gnLayerFilters.selected);

            scope.legends = [];
            for (var i = 0; i < fLayers.length; i++) {
              scope.legends.push({
                title: fLayers[i].get('label'),
                legend: fLayers[i].get('legend')
              });
            }
          });
        }
      };
    }]);

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
          scope.layerFilterFn = gnLayerFilters.selected;

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
          scope.layerFilterFn = gnLayerFilters.selected;
        }
      };
    }]);

})();
