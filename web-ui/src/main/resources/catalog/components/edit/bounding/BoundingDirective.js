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
  goog.provide('gn_bounding_directive');


  var module = angular.module('gn_bounding_directive', []);

  /**
   * @ngdoc directive
   * @name gn_bounding.directive:gnBoundingPolygon
   *
   * @description
   * This directive gives the user the possibility to define a bounding polygon,
   * either by drawing it manually on a map or copy-pasting data in the desired 
   * format. The user can also select an input projection.
   * The directive has a hidden output in GML & EPSG:4326.
   */
  module.directive('gnBoundingPolygon', [
    function() {
      return {
        restrict: 'E',
        scope: {
        },
        templateUrl: '../../catalog/components/edit/bounding/' +
          'partials/boundingpolygon.html',
        link: function (scope, element) {
        },
        controllerAs: 'ctrl',
        bindToController: true,
        controller: [
          'gnMap',
          'gnOwsContextService',
          'gnViewerSettings',
          function BoundingPolygonController(
            gnMap,
            gnOwsContextService,
            gnViewerSettings) {
            // init map
            this.map = new ol.Map({
              layers: [
                gnMap.getLayersFromConfig()
              ],
              view: new ol.View({})
            });

            // uses default context from database
            var contextUrl = gnViewerSettings.mapConfig.map ||
              '../../map/config-viewer.xml';
            gnOwsContextService.loadContextFromUrl(contextUrl, this.map);

            // projection list
            this.projections = gnMap.getMapConfig().projectionList;
            this.currentProjection = this.projections[0].code;

            // available input formats
            this.formats = [ 'GML', 'WKT', 'GeoJSON' ];
            this.currentFormat = this.formats[0];
          }
        ]
      };
    }]);
})();
