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

                // Get projection code of current map view and define polar coordinates in it
                var proj = scope.map.getView().getProjection().getCode();
                var southPole = (new ol.geom.Point([0, -90])).transform('EPSG:4326', proj).getFirstCoordinate();
                var northPole = (new ol.geom.Point([0, 90])).transform('EPSG:4326', proj).getFirstCoordinate();

                /**
                * Checks if the feature geometry contains the north or south pole.
                * Returns the polar coordinate in the current map projection when found or null otherwise.
                * NOTE: Will not trigger if pole is on the edge.
                * @param {ol.feature} feat
                */
                var getInsidePole = function(feat) {
                  let geom = feat.getGeometry();
                  if (geom.intersectsCoordinate(southPole)) {
                    return southPole;
                  } else if (geom.intersectsCoordinate(northPole)) {
                    return northPole;
                  }
                }

                /**
                * Adds points to line segments.
                * Specify the amount of segment parts to create (default = 4).
                * The number of points that will be added is (parts - 1).
                * @param {ol.feature} feat
                * @param {int} parts
                */
                var densifyEdges = function(feat, parts=4) {
                  let outCoords = [];
                  let inCoords = feat.getGeometry().getCoordinates()[0];
                  for (let i = 0; i < inCoords.length - 1; i++) {
                    let p0, p1, x0, y0, dX;
                    p0 = inCoords[i];
                    p1 = inCoords[i+1];
                    x0 = p0[0];
                    y0 = p0[1];
                    dX = (p1[0] - x0) / parts;
                    dY = (p1[1] - y0) / parts;
                    outCoords.push(p0);
                    for (let j = 1; j < parts; j++) {
                      // Add coords
                      outCoords.push([x0 + (j * dX), y0 + (j * dY)]);
                    }
                  }
                  // Add first coord to close polygon
                  outCoords.push(inCoords[0]);
                  feat.setGeometry(new ol.geom.Polygon([outCoords]));
                }

                /**
                * Makes a "cut" starting from the geometry edge straight up or down to the polar coordinate.
                * Depending on the pole, the chosen edge is at the upper (north) or lower (south) side.
                * The feature geometry is also transformed to WGS 1984 (EPSG:4326).
                * NOTE: this is not suitable for complex or multipart polygons!
                * 
                *           North pole polygon                   South pole polygon
                *          +======+===========+                 +------------------+
                *          |      |           |                 |                  |
                *          |      o           |                 |            o     |
                *          |                  |                 |            |     |
                *          +------------------+                 +============+=====+
                * 
                *   === -> "cut side" 
                *   o   -> polar coordinate
                * @param {ol.feature} feat
                * @param polarCoord
                */
                var getPolarExtent = function(feat, polarCoord) {
                  const atNorthPole = polarCoord == northPole;
                  const polarX = polarCoord[0];
                  const polarY = polarCoord[1];
                  const polarLat = atNorthPole ? 90 : -90;
                  let parts = feat.getGeometry().getCoordinates();

                  console.log('Search extent contains ' + (atNorthPole ? 'north' : 'south') + ' pole');

                  if (parts.length > 1) {
                    console.warn('Multi-polygon polar search extents are not supported: this will produce unexpected results');
                  }

                  // Find (1st) polar-side segment that crosses polar X at the "cut side".
                  // NOTE: this also works for non-square shapes, but NOT for shapes that cross polar X at multiple "cut sides"!
                  let coords = parts[0];
                  let crossingSegment = {
                    insertPos: -1,
                    firstPoint: null,
                    lastPoint: null
                  };
                  let area = 0;
                  for (let i = 0; i < coords.length - 1; i++) {
                    const p1 = coords[i];
                    const p2 = coords[i + 1];
                    area += (p2[0] - p1[0]) * (p1[1] + p2[1]);
                    if (!((p1[0] < polarX && polarX < p2[0]) || (p1[0] > polarX && polarX > p2[0]))) {
                      // Segment does not cross polar X: move to next
                      continue;
                    } else if (p1[0] == polarX && ((atNorthPole && p1[1] >= polarY) || (!atNorthPole && p1[1] <= polarY))) {
                      // P1 is on polar X at "good" side: no need to continue
                      crossingSegment.insertPos = i;
                      break;
                    } else if (p2[0] == polarX && ((atNorthPole && p2[1] >= polarY) || (!atNorthPole && p2[1] <= polarY))) {
                      // P2 is on polar X at "good" side: no need to continue
                      crossingSegment.insertPos = i + 1;
                      break;
                    }
                    // Check if segment is at the "good" side and store it
                    const ltr = p1[0] < p2[0];
                    if (atNorthPole && (p1[1] >= polarY || p2[1] >= polarY)) {
                      // North pole segment that crosses polar X
                      if (crossingSegment.firstPoint) {
                        // Crossing segment already found: 
                        // if there's another one, this would produce multi-polygons (not implemented)
                        console.warn('Complex polar search extents are not supported: this will produce unexpected results');
                        break;
                      }
                      crossingSegment.insertPos = ltr ? i + 2 : i + 1;
                      crossingSegment.firstPoint = ltr ? p1 : p2;
                      crossingSegment.lastPoint = ltr ? p2 : p1;
                    }
                    if (!atNorthPole && (p1[1] <= polarY || p2[1] <= polarY)) {
                      // South pole segment that crosses polar X
                      if (crossingSegment.firstPoint) {
                        // Crossing segment already found: 
                        // if there's another one, this would produce multi-polygons (not implemented)
                        console.warn('Complex polar search extents are not supported: this will produce unexpected results');
                        break;
                      }
                      crossingSegment.insertPos = ltr ? i + 1 : i + 2;
                      crossingSegment.firstPoint = ltr ? p2 : p1;
                      crossingSegment.lastPoint = ltr ? p1 : p2;
                    }
                  }

                  // Determine if shape coordinates are in clockwise order or not
                  const isCCW = area < 0;
                  
                  // Geometry contains no coordinate on polar X: calculate and add it
                  if (crossingSegment.firstPoint !== null) {
                    // Calculate point where polar X intersects the coordinate ring
                    const dX = crossingSegment.lastPoint[0] - crossingSegment.firstPoint[0];
                    const dY = crossingSegment.lastPoint[1] - crossingSegment.firstPoint[1];
                    const dXPole = polarCoord[0] - crossingSegment.firstPoint[0];
                    const offsetY = (dXPole / dX) * dY;
                    // Add that point to the coordinate ring
                    let centerCoord = [polarX, atNorthPole ? crossingSegment.firstPoint[1] - offsetY : crossingSegment.firstPoint[1] + offsetY];
                    coords.splice(crossingSegment.insertPos, 0, centerCoord);
                    feat.setGeometry(new ol.geom.Polygon([coords]));
                  }

                  // Transform to WGS 1984 (EPSG:4326)
                  feat.getGeometry().transform(proj, 'EPSG:4326');
                  
                  // Get all transformed coordinates again, set longitude of inserted one to (-)180
                  if (crossingSegment.insertPos >= 0) {
                    let lon1 = 180;
                    if ((atNorthPole && !isCCW) || (!atNorthPole && isCCW)) lon1 = -180;
                    const lon2 = -lon1; 
                    coords = feat.getGeometry().getCoordinates()[0];
                    coords[crossingSegment.insertPos][0] = lon1;
                    const lat = coords[crossingSegment.insertPos][1];
                    // Insert 3 coordinates at [lon1, (-)90], [lon2, (-)90] and [lon2, lat]
                    coords.splice(crossingSegment.insertPos + 1, 0, [lon1, polarLat], [lon2, polarLat], [lon2, lat]);
                    feat.setGeometry(new ol.geom.Polygon([coords]));
                  }
                }


                // Transform extent to WGS 1984 (EPSG:4326)
                var lonlatFeat, writer, proj, wkt;
                lonlatFeat = feature.clone();
                if (proj !== 'EPSG:4326') {
                  if (proj !== 'EPSG:3857') {
                    // Densify edges so we get a more accurate reprojection (relates to issue #4810)
                    densifyEdges(lonlatFeat);
                  }
                  // Check if original extent includes a pole
                  let polarCoord = getInsidePole(feature);
                  if (polarCoord) 
                  {
                    // Original extent includes a north or south pole: manipulate extent.
                    // Solves issue #4810: Query by south pole area doesn't give any results.
                    getPolarExtent(lonlatFeat, polarCoord);
                  } else {
                    // No pole included so a basic transformation to WGS 1984 (EPSG:4326) will do
                    lonlatFeat.getGeometry().transform(proj, 'EPSG:4326');
                  }
                }

                // Write the extent as 4326 WKT polygon
                writer = new ol.format.WKT();
                wkt = writer.writeFeature(lonlatFeat);
                bboxSet(parent, wkt);
              };

              // If given extent coords are given through attributes,
              // display the bbox on the map
              let coords = scope.$eval(attrs['gnDrawBboxExtent']);
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
