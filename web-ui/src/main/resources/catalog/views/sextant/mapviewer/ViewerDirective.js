(function() {
  goog.provide('sxt_viewer_directive');

  var module = angular.module('sxt_viewer_directive', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('sxtMainViewer', [
    'gnMap',
    function(gnMap) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/mapviewer/mainviewer.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs['map']);

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** print definition */
              scope.activeTools = {};

              scope.zoom = function(map, delta) {
                gnMap.zoom(map, delta);
              };
              scope.zoomToMaxExtent = function(map) {
                map.getView().setResolution(gnMapConfig.maxResolution);
              };

              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

            },
            post: function postLink(scope, iElement, iAttrs, controller) {
              //TODO: find another solution to render the map
              setTimeout(function() {
                scope.map.updateSize();
              }, 100);
            }
          };
        }
      };
    }]);
})();
