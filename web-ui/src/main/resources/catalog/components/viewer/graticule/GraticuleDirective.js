(function() {
  goog.provide('gn_graticule');

  var module = angular.module('gn_graticule', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGraticuleBtn
   *
   * @description
   * This directive provides a button to display ol3 graticule on the map.
   * The graticule style is no customisable for the moment.
   */
  module.directive('gnGraticuleBtn', [function() {

    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/graticule/partials/' +
          'graticule.html',
      link: function(scope, element, attrs) {

        var map = scope.$eval(attrs['gnGraticuleBtn']);

        var graticule = new ol.Graticule({
          strokeStyle: new ol.style.Stroke({
            color: 'rgba(255,120,0,0.9)',
            width: 2,
            lineDash: [0.5, 4]
          })
        });

        Object.defineProperty(graticule, 'active', {
          get: function() {
            return !!graticule.getMap();
          },
          set: function(val) {
            if (val) {
              graticule.setMap(map);
            } else {
              graticule.setMap(null);
            }
          }
        });
        scope.graticule = graticule;
      }
    };
  }]);
})();
