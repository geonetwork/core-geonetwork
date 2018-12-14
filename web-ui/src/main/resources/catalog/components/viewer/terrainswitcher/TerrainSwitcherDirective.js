/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
          // scope.terrains = ['default', 'none'];
          scope.terrains = ['none'];
          scope.currentTerrain = null;
          scope.dropup = angular.isDefined(attrs.dropup);


          scope.setTerrain = function(terrain) {
            scope.currentTerrain = terrain;
            if (scope.ol3d) {
              var scene = scope.ol3d.getCesiumScene();
              // https://github.com/geonetwork/core-geonetwork/issues/3317
              // Since September 2018, terrain provider is not available anymore
              // We have to move to ION services from Cesium which requires a token
              // to get access to.
              // Turn it off for now.
              // if (terrain === 'default') {
              //   scene.terrainProvider = new Cesium.CesiumTerrainProvider({
              //     url: '//assets.agi.com/stk-terrain/world'
              //     // url: Cesium.IonResource.fromAssetId(1)
              //   });
              // } else {
              // }
              scene.terrainProvider = new Cesium.EllipsoidTerrainProvider();
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
