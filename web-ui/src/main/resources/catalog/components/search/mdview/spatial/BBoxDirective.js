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
  goog.provide('gn_bbox_directive');


  var module = angular.module('gn_bbox_directive', []);

  /**
   * @ngdoc directive
   * @name gn_bbox_directive.directive:gnBBox
   *
   * @description
   * This directive defines a bounding box to be displayed by the enclosing spatial viewer directive.
   *
   * @attribute {numeric} eastBL east bounding longitude
   * @attribute {numeric} westBL west bounding longitude
   * @attribute {numeric} northBL north bounding latitude
   * @attribute {numeric} southBL south bounding latitude
   */
  module.directive('gnBBox', [
    function() {
        return {
        restrict: 'E',
        scope: {
          eastbl: '@',
          westbl: '@',
          northbl: '@',
          southbl: '@'
        },
        require: '^gnSpatialViewer',
        link: function(scope, elem, attrs, spatialCtrl) {
            // parse attributes
            try {
                var eastBL = Number(scope.eastbl);
                var westBL = Number(scope.westbl);
                var northBL = Number(scope.northbl);
                var southBL = Number(scope.southbl);
            } catch (e) {
                console.warn('Could not parse bounding box');
                console.warn(e);
                return;
            }

            // create geometry
            var geometry = new ol.geom.Polygon([[
                [westBL, southBL],[westBL, northBL],
                [eastBL, northBL],[eastBL, southBL],
                [westBL, southBL]
            ]]);

            // transform to output (map) projection
            var outputProjection = spatialCtrl.map.getView().getProjection();
            var sourceProjection = "CRS:84";

            try {
                geometry.transform(sourceProjection, outputProjection);
            } catch (e) {
                console.warn('Could not transform bounding box to map projection');
                console.warn(e);
                return;
            }

            //create feature
            var feature = new ol.Feature({
                geometry: geometry
            });

            spatialCtrl.addFeature(feature)
        }
      };
    }]);
})();
