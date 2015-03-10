(function() {
  goog.provide('gn_baselayerswitcher_directive');

  var module = angular.module('gn_baselayerswitcher_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_baselayerswitcher_directive.directive:gnBaselayerswitcher
   *
   * @description
   * Provides a button and a dropdown menu to switch background layer of the
   * given map
   */
  module.directive('gnBaselayerswitcher', [
    'gnViewerSettings',
    function(gnViewerSettings) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/baselayerswitcher/' +
            'partials/baselayerswitcher.html',
        scope: {
          map: '=gnBaselayerswitcherMap'
        },
        link: function(scope, element, attrs) {
          scope.layers = gnViewerSettings.bgLayers;
          scope.map.getLayers().insertAt(0, scope.layers[0]);
          scope.setBgLayer = function(layer) {
            layer.setVisible(true);
            var layers = scope.map.getLayers();
            layers.removeAt(0);
            layers.insertAt(0, layer);
            return false;
          };
        }
      };
    }]);

})();
