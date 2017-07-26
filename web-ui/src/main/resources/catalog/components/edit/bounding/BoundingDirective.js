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
   * 
   * @attribute {string} coordinates list of coordinates separated with spaces
   * @attribute {string} identifier id of the hidden input that will hold
   *  the value entered by the user
   */
  module.directive('gnBoundingPolygon', [
    function() {
      return {
        restrict: 'E',
        scope: {
          polygonXml: '@',
          identifier: '@'
        },
        templateUrl: '../../catalog/components/edit/bounding/' +
          'partials/boundingpolygon.html',
        link: {
          post: function (scope, element) {
            scope.ctrl.map.renderSync();
            scope.ctrl.initValue();
          }
        },
        controllerAs: 'ctrl',
        bindToController: true,
        controller: [
          '$scope',
          '$attrs',
          '$http',
          'gnMap',
          'gnOwsContextService',
          'gnViewerSettings',
          function BoundingPolygonController(
            $scope,
            $attrs,
            $http,
            gnMap,
            gnOwsContextService,
            gnViewerSettings) {
            // set read only
            this.readOnly = $scope.$eval($attrs['readOnly']);

            // init map
            this.map = new ol.Map({
              layers: [
                gnMap.getLayersFromConfig()
              ],
              view: new ol.View({
                center: [0, 0],
                projection: gnMap.getMapConfig().projection,
                zoom: 2
              })
            });

            // output for editor (equals input by default)
            this.outputPolygonXml = this.polygonXml;
            this.outputFormat = new ol.format.GML({
              featureNS: 'http://www.isotc211.org/2005/gmd',
              featureType: 'EX_BoundingPolygon',
              srsName: 'EPSG:4326',
              multiSurface: true
            });

            // projection list
            this.projections = gnMap.getMapConfig().projectionList;
            this.currentProjection = this.projections[0].code;

            // available input formats
            // GML is not available as it cannot be parsed without namespace info
            this.formats = [ 'WKT', 'GeoJSON' ];
            this.currentFormat = this.formats[0];

            // parse initial input coordinates to display shape (first in WKT)
            this.initValue = function () {
              if (this.polygonXml) {
                this.currentFormat = 'WKT';
                this.currentProjection = 'EPSG:4326';
                var formatWkt = new ol.format.WKT();

                // parse first feature from source XML & set geometry name
                var correctedXml = '<gml:featureMembers>' +
                  this.polygonXml
                    .replace('<gml:LinearRingTypeCHOICE_ELEMENT0>', '')
                    .replace('</gml:LinearRingTypeCHOICE_ELEMENT0>', '')
                  + '</gml:featureMembers>';
                var feature = this.outputFormat.readFeatures(correctedXml)[0];
                feature.setGeometryName('polygon');

                // write feature geometry
                this.inputGeometry = formatWkt.writeGeometry(
                  feature.getGeometry(),
                  {
                    featureProjection: 'EPSG:4326'
                  }
                );
              }
            }.bind(this);

            // this will receive errors from the geometry tool input parsing
            this.parseError = null;
            this.parseErrorHandler = function (error) {
              this.parseError = error;
            }.bind(this);

            // outputs gm for the editor
            $scope.$watch('ctrl.outputGeometry', function (geometry) {
              if (!geometry) {
                return;
              }
              if (!geometry instanceof ol.geom.Polygon) {
                console.error('Error in gn-bounding-polygon: Geometry should ' +
                  'be a Polygon; aborting.');
                return;
              }

              // create a new feature & print the GML
              var feature = new ol.Feature({
                'polygon': geometry
              });
              feature.setGeometryName('polygon');
              this.outputPolygonXml = this.outputFormat.writeFeaturesNode(
                [feature]).innerHTML;
            }.bind(this));
          }
        ]
      };
    }]);
})();
