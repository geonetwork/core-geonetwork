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
          'ngeoDecorateInteraction',
          'gnGeometryService',
          function BoundingPolygonController(
            $scope,
            $attrs,
            $http,
            gnMap,
            gnOwsContextService,
            gnViewerSettings,
            ngeoDecorateInteraction,
            gnGeometryService) {
            var ctrl = this;

            // set read only
            ctrl.readOnly = $scope.$eval($attrs['readOnly']);

            // init map
            ctrl.map = new ol.Map({
              layers: [
                gnMap.getLayersFromConfig()
              ],
              view: new ol.View({
                center: [0, 0],
                projection: gnMap.getMapConfig().projection,
                zoom: 2
              })
            });

            // interactions with map
            var layer = gnGeometryService.getCommonLayer(ctrl.map);
            var source = layer.getSource();

            ctrl.drawInteraction = new ol.interaction.Draw({
              type: 'MultiPolygon',  // TODO: handle linestrings
              source: source,
              geometryName: 'polygon'
            });
            ctrl.modifyInteraction = new ol.interaction.Modify({
              features: source.getFeaturesCollection()
            });

            // add our layer&interactions to the map
            ctrl.map.addInteraction(ctrl.drawInteraction);
            ctrl.map.addInteraction(ctrl.modifyInteraction);
            ctrl.drawInteraction.setActive(false);
            ctrl.modifyInteraction.setActive(false);
            ngeoDecorateInteraction(ctrl.drawInteraction);
            ngeoDecorateInteraction(ctrl.modifyInteraction);

            // clear existing features on draw end & save feature
            ctrl.drawInteraction.on('drawend', function(event) {
              source.clear(event.feature);
              ctrl.updateOutput(event.feature);
              ctrl.drawInteraction.setActive(false);
            });

            // update output on modify end
            ctrl.modifyInteraction.on('modifyend', function(event) {
              ctrl.updateOutput(event.features.item(0));
            });

            // output for editor (equals input by default)
            ctrl.outputPolygonXml = ctrl.polygonXml;

            // projection list
            ctrl.projections = gnMap.getMapConfig().projectionList;
            ctrl.currentProjection = ctrl.projections[0].code;

            // available input formats
            // GML is not available as it cannot be parsed without namespace info
            ctrl.formats = [ 'WKT', 'GeoJSON' ];
            ctrl.currentFormat = ctrl.formats[0];

            // parse initial input coordinates to display shape (first in WKT)
            ctrl.initValue = function () {
              console.log('received xml: ' + ctrl.polygonXml);

              if (ctrl.polygonXml) {
                // parse first feature from source XML & set geometry name
                var correctedXml = '<gml:featureMembers>' +
                  ctrl.polygonXml
                    .replace('<gml:LinearRingTypeCHOICE_ELEMENT0>', '')
                    .replace('</gml:LinearRingTypeCHOICE_ELEMENT0>', '')
                  + '</gml:featureMembers>';
                var feature = gnGeometryService.parseGeometryInput(
                  ctrl.map,
                  correctedXml,
                  {
                    crs: 'EPSG:4326',
                    format: 'gml',
                    gmlFeatureNS: 'http://www.isotc211.org/2005/gmd',
                    gmlFeatureElement: 'EX_BoundingPolygon',
                  }
                )[0];
                feature.setGeometryName('polygon');

                // fit view
                ctrl.map.getView().fit(feature.getGeometry(),
                  ctrl.map.getSize());

                // add to map
                source.clear();
                source.addFeature(feature);
              }
            };

            // update output with gml
            ctrl.updateOutput = function (feature) {
              // fit view
              ctrl.map.getView().fit(feature.getGeometry(),
                ctrl.map.getSize());

              // print output
              ctrl.outputPolygonXml = gnGeometryService.printGeometryOutput(
                ctrl.map,
                feature,
                {
                  crs:'EPSG:4326',
                  format: 'gml',
                  gmlFeatureNS: 'http://www.isotc211.org/2005/gmd',
                  gmlFeatureElement: 'EX_BoundingPolygon'
                }
              );
              console.log('xml output: ' + ctrl.outputPolygonXml);
            };

            // this will receive errors from the geometry tool input parsing
            ctrl.parseError = null;

            // watches input change & outputs gml for the editor
            $scope.$watch(
              function () {
                return ctrl.inputGeometry + ctrl.currentFormat +
                  ctrl.currentProjection;
              }, function () {
              if (!ctrl.inputGeometry) {
                return;
              }

              // parse geometry
              try {
                var geometry = gnGeometryService.parseGeometryInput(
                  ctrl.map,
                  ctrl.inputGeometry,
                  {
                    crs: ctrl.currentProjection,
                    format: ctrl.currentFormat,
                  }
                );
              } catch (e) {
                ctrl.parseError = e.message;
              }

              // create a new feature & print the GML
              var feature = new ol.Feature({
                'polygon': geometry
              });
              feature.setGeometryName('polygon');
              source.clear();
              source.addFeature(feature);
              ctrl.updateOutput(feature);
            });
          }
        ]
      };
    }]);
})();
