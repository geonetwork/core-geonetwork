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

  goog.provide('gn_map_field_directive');

  angular.module('gn_map_field_directive', [])
      .directive('gnMapField', [
        'gnMap',
        function(gnMap) {
          return {
            restrict: 'A',
            scope: true,
            templateUrl: function(elem, attrs) {
              return attrs.template || '../../catalog/components/search/map/' +
                  'partials/mapfield.html';
            },
            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope, iElement, iAttrs, controller) {

                  scope.map = scope.$eval(iAttrs['gnMapField']);
                  scope.gnDrawBboxBtn = iAttrs['gnMapFieldGeom'];
                  scope.gnDrawBboxExtent = iAttrs['gnMapFieldExtent'];

                  // get list of relation types
                  // [overlaps encloses fullyOutsideOf fullyEnclosedWithin
                  // intersection crosses touches within]

                  var opt = scope.$eval(iAttrs['gnMapFieldOpt']) || {};
                  scope.relations = opt.relations;

                  scope.gnMap = gnMap;

                  /**
                   * Fit map view to map projection max extent
                   */
                  scope.maxExtent = function() {
                    scope.map.getView().fit(scope.map.getView().
                            getProjection().getExtent(), scope.map.getSize());
                  };

                  /**
                   * When the geomtry is updated, set this value in
                   * scope.currentExtent and remove relation param if
                   * geometry is null.
                   */
                  scope.$watch(scope.gnDrawBboxBtn, function(v) {
                    if (!v) {
                      delete scope.searchObj.params.relation;
                    }
                    scope.currentExtent = scope.$eval(scope.gnDrawBboxBtn);
                  });

                  /**
                   * Set active relation (intersect, within, etc..). Run search
                   * when changed.
                   */
                  scope.setRelation = function(rel) {
                    scope.searchObj.params.relation = rel;
                    if (!!scope.searchObj.params.geometry) {
                      scope.triggerSearch();
                    }
                  };
                }
              };
            }
          };
        }
      ])

      .directive('gnDrawBboxBtn', [
        'olDecorateInteraction',
        '$parse',
        '$translate',
        'gnSearchSettings',
        'gnMap',
        function(olDecorateInteraction, $parse, $translate,
                 gnSearchSettings) {
          return {
            restrict: 'A',
            scope: true,
            controller: ['$scope', function($scope) {
              var dragbox = new ol.interaction.DragBox({
                style: gnSearchSettings.olStyles.drawBbox
              });
              olDecorateInteraction(dragbox, $scope.map);
              dragbox.active = false;
              $scope.map.addInteraction(dragbox);
              $scope.interaction = dragbox;
            }],
            link: function(scope, element, attrs) {

              var parent = scope.$parent.$parent;

              // Assign drawn extent to given scope property
              var bboxGet = $parse(attrs['gnDrawBboxBtn']);
              var bboxSet = bboxGet.assign;

              // Create overlay to persist the bbox
              var feature = new ol.Feature();
              var featureOverlay = new ol.layer.Vector({
                source: new ol.source.Vector(),
                map: scope.map,
                style: gnSearchSettings.olStyles.drawBbox
              });
              featureOverlay.getSource().addFeature(feature);

              /**
               * Update extent scope value with the WKT polygon
               * @param {ol.geometry} geom
               */
              var updateField = function(geom) {
                feature.setGeometry(geom);

                // Write the extent as 4326 WKT polygon
                var lonlatFeat, writer, wkt;
                lonlatFeat = feature.clone();
                lonlatFeat.getGeometry().transform(
                    scope.map.getView().getProjection().getCode(),
                    'EPSG:4326');
                writer = new ol.format.WKT();
                wkt = writer.writeFeature(lonlatFeat);
                bboxSet(parent, wkt);
              };

              // If given extent coords are given through attributes,
              // display the bbox on the map
              var coords = scope.$eval(attrs['gnDrawBboxExtent']);
              if (coords) {
                updateField(new ol.geom.Polygon(coords));
              }
              scope.getButtonTitle = function() {
                if (scope.interaction.active) {
                  return $translate.instant('clickToRemoveSpatialFilter');
                } else {
                  return $translate.instant('drawAnExtentToFilter');
                }
              };
              scope.interaction.on('boxend', function() {
                scope.$apply(function() {
                  updateField(scope.interaction.getGeometry());
                  scope.triggerSearch();
                });
              });

              function resetSpatialFilter() {
                feature.setGeometry(null);
                bboxSet(parent, '');
                scope.map.render();
              }
              // Remove the bbox when the interaction is not active
              scope.$watch('interaction.active', function(v, o) {
                if (!v && o) {
                  resetSpatialFilter();
                  if (!!scope.searchObj.params.geometry) {
                    scope.triggerSearch();
                  }
                }
              });

              // When search form is reset, remove the geom
              scope.$on('beforeSearchReset', function(event, preserveGeometrySearch) {
                if (!preserveGeometrySearch) {
                  resetSpatialFilter();
                  scope.interaction.active = false;
                }
              });
            }
          };
        }
      ]);
})();
