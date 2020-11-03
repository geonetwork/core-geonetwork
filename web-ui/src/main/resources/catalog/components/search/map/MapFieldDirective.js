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

                // Get projection code of current map view
                var proj = scope.map.getView().getProjection().getCode();

                /**
                 * Transforms the given longitude-latitude pair to XY coordinates in the current projection.
                 * @param {number} lon 
                 * @param {number} lat 
                 */
                var getXY = function(lon, lat) {
                  return (new ol.geom.Point([lon, lat])).transform('EPSG:4326', proj).getFirstCoordinate();
                }

                // Define polar coordinates in current projection
                var southPole = getXY(0, -90);
                var northPole = getXY(0, 90);

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
                  // No need to densify if shape already has a reasonable amount of points
                  if (inCoords.length > 4 * parts) return;
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
                 * Given three colinear points (p1, p2, p3), check if p2 lies on line segment p1-p3.
                 */ 
                var onSegment = function(p1, p2, p3) {
                  if (p2[0] <= Math.max(p1[0], p3[0]) && p2[0] >= Math.min(p1[0], p3[0]) && 
                      p2[1] <= Math.max(p1[1], p3[1]) && p2[1] >= Math.min(p1[1], p3[1])) {
                        return true;
                  }
                  return false;
                }

                /**
                 * Finds the orientation of an ordered triplet (p1, p2, p3). 
                 * Returns one of the following values: 
                 * 0 : Colinear points 
                 * 1 : Clockwise points 
                 * 2 : Counterclockwise 
                 *
                 * See https://www.geeksforgeeks.org/orientation-3-ordered-points/amp for details of below formula.
                 */ 
                var getOrientation = function(p1, p2, p3) {
                  let value = ((p2[1] - p1[1]) * (p3[0] - p2[0])) - ((p2[0] - p1[0]) * (p3[1] - p2[1]));
                  if (value > 0) return 1;
                  else if (value < 0) return 2;
                  return 0;
                }

                /**
                 * Returns true if the line segments 'line1' and 'line2' (with points A and B) intersect. 
                 */
                var linesIntersect = function(line1A, line1B, line2A, line2B) {
                  // Find the 4 orientations required for the general and special cases 
                  let o1 = getOrientation(line1A, line1B, line2A);
                  let o2 = getOrientation(line1A, line1B, line2B);
                  let o3 = getOrientation(line2A, line2B, line1A);
                  let o4 = getOrientation(line2A, line2B, line1B);

                  // General case
                  if ((o1 !== o2) && (o3 !== o4)) return true;

                  // Special cases:
                  // Line1(A,B) and Line2(A) are colinear and Line2(A) lies on Line1
                  if ((o1 === 0) && onSegment(line1A, line2A, line1B)) return true;
                  // Line1(A,B) and Line2(B) are colinear and Line2(B) lies on Line1
                  if ((o2 === 0) && onSegment(line1A, line2B, line1B)) return true;
                  // Line2(A,B) and Line1(A) are colinear and Line1(A) lies on Line2
                  if ((o3 === 0) && onSegment(line2A, line1A, line2B)) return true;
                  // Line2(A,B) and Line1(B) are colinear and Line1(B) lies on Line2
                  if ((o4 === 0) && onSegment(line2A, line1B, line2B)) return true;
                  // Nothing intersects
                  return false;                  
                }

                /**
                 * Calculates the intersection point between 'line1' and 'line2'.
                 * Assumes that the lines actually intersect (use linesIntersect() to verify).
                 */
                var getIntersection = function(line1A, line1B, line2A, line2B) {
                  // Line 1 represented as a1x + b1y = c1 
                  let a1 = line1B[1] - line1A[1]; 
                  let b1 = line1A[0] - line1B[0]; 
                  let c1 = a1 * line1A[0] + b1 * line1A[1]; 
       
                  // Line 2 represented as a2x + b2y = c2 
                  let a2 = line2B[1] - line2A[1]; 
                  let b2 = line2A[0] - line2B[0]; 
                  let c2 = a2 * line2A[0] + b2 * line2A[1]; 
                
                  let determinant = a1 * b2 - a2 * b1;                
                  let x = (b2 * c1 - b1 * c2) / determinant; 
                  let y = (a1 * c2 - a2 * c1) / determinant; 
                  return [x, y];
                }

                /**
                 * Makes a "cut" at the datum line from the geometry edge up or down to the polar coordinate.
                 * The feature geometry is also transformed to WGS 1984 (EPSG:4326).
                 * NOTE: This is not suitable for complex or multipart polygons!
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
                var modifyPolarExtent = function(feat, polarCoord) {
                  const atNorthPole = polarCoord == northPole;
                  const polarLat = atNorthPole ? 90 : -90;
                  const parts = feat.getGeometry().getCoordinates();

                  // Set the point of origin at equator (where datum line towards polar coordinate starts)
                  let originCoord = getXY(180, 0);

                  // Show some warnings/info (when in debug mode)
                  console.log('Search extent contains ' + (atNorthPole ? 'north' : 'south') + ' pole');
                  if (parts.length > 1) {
                    console.warn('Multi-polygon polar search extents are not supported: this will produce unexpected results');
                  }

                  // Find (1st) polar-side line segment that crosses the "datum".
                  // NOTE: this also works for non-square shapes, but NOT for shapes that cross the datum line multiple times!
                  let coords = parts[0];
                  let intersectionPointIndex = -1;
                  let lineStart = null;
                  let lineEnd = null;
                  for (let i = 0; i < coords.length - 1; i++) {
                    const p1 = coords[i];
                    const p2 = coords[i + 1];
                    if (linesIntersect(p1, p2, originCoord, polarCoord)) {
                      intersectionPointIndex = i + 1;
                      lineStart = p1;
                      lineEnd = p2;
                      break;
                    }
                  }
                  
                  if (lineStart !== null) {
                    // Calculate intersection point between extent segment and datum line
                    let intersectionPoint = getIntersection(lineStart, lineEnd, originCoord, polarCoord);
                    coords.splice(intersectionPointIndex, 0, intersectionPoint);
                    feat.setGeometry(new ol.geom.Polygon([coords]));
                  }

                  // Transform to WGS 1984 (EPSG:4326)
                  feat.getGeometry().transform(proj, 'EPSG:4326');
                  
                  if (intersectionPointIndex >= 0) {
                    // Get all (transformed) coordinates and previous and next longitudes
                    coords = feat.getGeometry().getCoordinates()[0];
                    const prevLon = coords[intersectionPointIndex - 1][0];
                    const nextLon = coords[intersectionPointIndex + 1][0];
                    // Determine polar longitudes (order)
                    const polarLon1 = prevLon < 0 && nextLon > 0 ? -180 : 180;
                    const polarLon2 = -polarLon1;
                    // Get longitude of inserted coordinate, negate if needed (ensure correct hemisphere)
                    let insertedLon = coords[intersectionPointIndex][0];
                    if ((insertedLon > 0 && polarLon1 < 0) || (insertedLon < 0 && polarLon1 > 0)) {
                      insertedLon = -insertedLon;
                      coords[intersectionPointIndex][0] = insertedLon;
                    }                
                    // Get latitude of inserted coordinate
                    const insertedLat = coords[intersectionPointIndex][1];
                    // Insert 3 coordinates (2 at either side of the pole) to create the "cut"
                    coords.splice(intersectionPointIndex + 1, 0, [polarLon1, polarLat], [polarLon2, polarLat], [-insertedLon, insertedLat]);
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
                    modifyPolarExtent(lonlatFeat, polarCoord);
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
