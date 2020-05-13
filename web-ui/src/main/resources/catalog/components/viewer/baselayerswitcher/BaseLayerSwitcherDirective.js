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
  goog.provide('gn_baselayerswitcher');

  var module = angular.module('gn_baselayerswitcher', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnBaselayerswitcher
   *
   * @description
   * Provides a button and a dropdown menu to switch background layer of the
   * given map
   */
  module.directive('gnBaselayerswitcher', [
    'gnViewerSettings', 'gnOwsContextService', '$rootScope',
    function(gnViewerSettings, gnOwsContextService, $rootScope) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/baselayerswitcher/' +
            'partials/baselayerswitcher.html',
        scope: {
          map: '=gnBaselayerswitcherMap'
        },
        link: function(scope, element, attrs) {
          scope.layers = gnViewerSettings.bgLayers;
          scope.dropup = angular.isDefined(attrs.dropup);
          var firstLayer = scope.map.getLayers().item(0);
          if(firstLayer && scope.layers.indexOf(firstLayer) < 0 &&
            !scope.layers.fromCtx) {
            scope.map.getLayers().insertAt(0, scope.layers[0]);
          }
          scope.setBgLayer = function(layer) {
            layer.setVisible(true);
            var layers = scope.map.getLayers();
            if(layers.getLength() > 0) {
              layers.item(0).set("currentBackground", false)
              layers.removeAt(0);
            }
            layers.insertAt(0, layer);
            layer.set("currentBackground", true);
            return false;
          };


          scope.changeBackground = function (layer) {
            scope.setBgLayer(layer);
          };
          
          scope.$watch(function() { return gnViewerSettings.bgLayers}, 
              function(bgLayers) {
                if(bgLayers && bgLayers.length && bgLayers.length > 0 ) {
                  scope.layers = bgLayers;
                  
                  //Do we remember the previous background layer?
                  var i = 0;
                  var j = 0;
                  bgLayers.forEach(function(layer) {
                    if(layer.get("currentBackground")) {
                      i = j;
                    }
                    j++;
                  });
                  
                  scope.setBgLayer(scope.layers[i]);
                }
          });

          scope.reset = function() {
            $rootScope.$broadcast('owsContextReseted');
            gnOwsContextService.loadContextFromUrl(
              gnViewerSettings.defaultContext,
              scope.map,
              gnViewerSettings.additionalMapLayers);
          };
        }
      };
    }]);

})();
