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
  goog.provide('gn_geometry_service');

  var module = angular.module('gn_geometry_service', []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_geometry.service:gnGeometryService
   * @requires gnMap
   *
   * @description
   * The `gnGeometryService` service provides utility related to handling
   * geometry and mime types
   */
  module.service('gnGeometryService', [
    function() {

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#getFormatFromMimeType
       *
       * @description
       * Guess the output format to use in a gn-geometry-tool directive based
       * on a mime type received from a DescribeProcess call.
       *
       * @param {string} mimeType mime type from the process description
       * @return {string} format to be used by a gn-geometry-tool directive
       */
      this.getFormatFromMimeType = function(mimeType) {
        var parts = mimeType.split(';');
        parts.forEach(function(p) {
          p = p.trim().toLowerCase();
        });

        switch (parts[0]) {
          case 'application/json':
            return 'geojson';

          case 'application/wkt':
            return 'wkt';

          case 'application/gml+xml':
          case 'text/xml':
            return 'gml';

          default:
            return 'object';
        }
      };

    }
  ]);
})();
