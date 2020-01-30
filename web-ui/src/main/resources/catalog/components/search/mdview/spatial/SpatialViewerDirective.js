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
  goog.provide('gn_spatial_viewer_directive');


  var module = angular.module('gn_spatial_viewer_directive', []);

  /**
   * @ngdoc directive
   * @name gn_spatial_viewer_directive.directive:gnSpatialViewer
   *
   * @description
   * This directive displays enclosed spatial elements on a map
   */
  module.directive('gnSpatialViewer', [
    function() {
      return {
        restrict: 'E',
        transclude: true,
        scope: {},
        templateUrl: '../../catalog/components/search/mdview/spatial/' +
            'partials/spatialviewer.html',
        controllerAs: 'ctrl',
        bindToController: true,
        controller: [
          'gnMap',
          'gnMapsManager',
          'gnGeometryService',
          function SpatialViewerController(
              gnMap,
              gnMapsManager,
              gnGeometryService) {
            var ctrl = this;

            // init map
            ctrl.map = gnMapsManager.createMap(gnMapsManager.EDITOR_MAP);

            // fit to view when ready
            ctrl.map.get('sizePromise').then(function() {
              ctrl.fitToView();
            });

            // get drawing layer
            var layer = gnGeometryService.getCommonLayer(ctrl.map);
            var source = layer.getSource();

            // api for adding features to the map (used by enclosed spatial elements)
            ctrl.addFeature = function(feature) {
              source.addFeature(feature)
            };

            // function to fit drawn features to view
            ctrl.fitToView = function() {
              ctrl.map.getView().fit(source.getExtent(),
                  ctrl.map.getSize());
            };
          }
        ]
      };
    }]);
})();
