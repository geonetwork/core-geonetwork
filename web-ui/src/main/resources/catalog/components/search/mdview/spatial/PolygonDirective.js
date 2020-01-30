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
  goog.provide('gn_polygon_directive');


  var module = angular.module('gn_polygon_directive', []);

  /**
   * @ngdoc directive
   * @name gn_polygon_directive.directive:gnPolygon
   *
   * @description
   * This directive defines a bounding polygon to be displayed by the enclosing spatial viewer directive.
   *
   * @attribute {string} polygonXml  bounding polygon in GML format
   */
  module.directive('gnPolygon', [
    'gnGeometryService',
    function(gnGeometryService) {
        return {
        restrict: 'E',
        scope: {
          polygonXml: '@'
        },
        require: '^gnSpatialViewer',
        link: function(scope, elem, attrs, spatialCtrl) {
            var dataProjection = gnGeometryService.getGmlProjection(
                scope.polygonXml
            );

            // parse first feature from source XML & set geometry name
            try {
                var geometry = gnGeometryService.parseGeometryInput(
                    spatialCtrl.map,
                    scope.polygonXml,
                    {
                        crs: dataProjection,
                        format: 'gml'
                    }
                );
            } catch (e) {
                console.warn('Could not parse geometry');
                console.warn(e);
            }

            if (!geometry) {
                console.warn('Could not parse geometry from extent polygon');
                return;
            }

            var feature = new ol.Feature({
                geometry: geometry
            });

            spatialCtrl.addFeature(feature)
        }
      };
    }]);
})();
