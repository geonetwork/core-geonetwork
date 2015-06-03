(function() {
  goog.provide('gn_terrainswitcher_directive');

  var module = angular.module('gn_terrainswitcher_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_terrainswitcher_directive.directive:gnterrainswitcher
   *
   * @description
   * Provides a button and a dropdown menu to switch 3D map terrain provider
   */
  module.directive('gnTerrainSwitcher', [
    'gnViewerSettings',
    function(gnViewerSettings) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/terrainswitcher/' +
            'partials/terrainswitcher.html',
        scope: {
          ol3d: '=gnTerrainSwitcher'
        },
        link: function(scope, element, attrs) {
          scope.terrains = ['default', 'none'];
          scope.currentTerrain = null;
          scope.dropup = angular.isDefined(attrs.dropup);


          scope.setTerrain = function(terrain) {
            scope.currentTerrain = terrain;
            if (scope.ol3d) {
              var scene = scope.ol3d.getCesiumScene();
              if (terrain === 'default') {
                scene.terrainProvider = new Cesium.CesiumTerrainProvider({
                  url: '//assets.agi.com/stk-terrain/world'
                });
              } else {
                scene.terrainProvider = new Cesium.EllipsoidTerrainProvider();
              }
            }
            return false;
          };

          // Initialize the terrain once the 3D mode is started
          scope.$watch('ol3d', function(newValue, oldValue) {
            if (newValue != oldValue) {
              scope.setTerrain(scope.terrains[0]);
            }
          });
        }
      };
    }]);

})();
