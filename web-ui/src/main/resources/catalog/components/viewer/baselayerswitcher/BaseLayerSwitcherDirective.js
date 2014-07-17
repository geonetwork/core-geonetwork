(function () {
  goog.provide('gn_baselayerswitcher_directive');

  var module = angular.module('gn_baselayerswitcher_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnBaselayerswitcher', [
    function () {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/viewer/baselayerswitcher/' +
        'partials/baselayerswitcher.html',
      scope: {
        map: '=gnBaselayerswitcherMap',
        layers: '=gnBaselayerswitcherLayers'
      },
      link: function (scope, element, attrs) {
        scope.map.getLayers().insertAt(0, scope.layers[0]);
        scope.setBgLayer = function(layer) {
          var layers = scope.map.getLayers();
          layers.removeAt(0);
          layers.insertAt(0, layer);
          return false;
        };
      }
    };
  }]);

})();
