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

(function () {

  goog.provide('gn_map_field_service');

  var module = angular.module('gn_map_field_service', []);

  module.factory('gnMapFieldService', 
    function () {  

      var LONLAT_WGS84 = 'EPSG:4326';   // Projection code of WGS 1984 coordinate system
      var WEB_MERCATOR = 'EPSG:3857';   // Projection code of (Google) Web Mercator
      var LL_SOUTHPOLE = [0, -90];      // South pole longitude-latitude coordinate
      var LL_NORTHPOLE = [0, 90];       // North pole longitude-latitude coordinate
      var LL_EQUATORDL = [180, 0];      // Longitude-latitude coordinate of datum line at equator

      /**
       * Checks if the feature geometry contains the north or south pole.
       * Returns the polar coordinate in the current map projection when found or null otherwise.
       * NOTE: Will not trigger if pole is on the edge.
       * @param {ol.feature} feat
       * @param {string} proj
       */
      var getContainedPole = function(feat, proj) {
        var geom = feat.getGeometry();
        var southPole = ol.proj.fromLonLat(LL_SOUTHPOLE, proj);
        var northPole = ol.proj.fromLonLat(LL_NORTHPOLE, proj);
        if (geom.intersectsCoordinate(southPole)) {
          console.debug('Search extent contains south pole');
          return southPole;
        } else if (geom.intersectsCoordinate(northPole)) {
          console.debug('Search extent contains north pole');
          return northPole;
        }
      }

      /**
       * Breaks each segment of an extent polygon into 4 parts.
       * The number of points that will be added to each segment is 3.
       * If the polygon has more than 5 coordinates, the function
       * assumes that it's not an extent and will not densify.
       * @param {ol.feature} feat
       */
      var densifyEdges = function(feat) {
        var outCoords = [];
        // Get first/outer ring
        var inCoords = feat.getGeometry().getCoordinates()[0];
        // Do not densify if shape does not appear to be an extent
        if (inCoords.length > 5) return;
        for (var i = 0; i < inCoords.length - 1; i++) {
          var p0, p1, x0, y0, dX;
          p0 = inCoords[i];
          p1 = inCoords[i+1];
          x0 = p0[0];
          y0 = p0[1];
          dX = (p1[0] - x0) / 4;
          dY = (p1[1] - y0) / 4;
          outCoords.push(p0);
          for (var j = 1; j < 4; j++) {
            // Add 3 coords
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
        var value = ((p2[1] - p1[1]) * (p3[0] - p2[0])) - ((p2[0] - p1[0]) * (p3[1] - p2[1]));
        if (value > 0) return 1;
        else if (value < 0) return 2;
        return 0;
      }

      /**
       * Returns true if the line segments 'line1' and 'line2' (with points A and B) intersect. 
       */
      var linesIntersect = function(line1A, line1B, line2A, line2B) {
        // Find the 4 orientations required for the general and special cases 
        var o1 = getOrientation(line1A, line1B, line2A);
        var o2 = getOrientation(line1A, line1B, line2B);
        var o3 = getOrientation(line2A, line2B, line1A);
        var o4 = getOrientation(line2A, line2B, line1B);

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
        var a1 = line1B[1] - line1A[1]; 
        var b1 = line1A[0] - line1B[0]; 
        var c1 = a1 * line1A[0] + b1 * line1A[1]; 

        // Line 2 represented as a2x + b2y = c2 
        var a2 = line2B[1] - line2A[1]; 
        var b2 = line2A[0] - line2B[0]; 
        var c2 = a2 * line2A[0] + b2 * line2A[1]; 
      
        // NOTE: Determinant could be 0 when lines are parallel,
        // but since we constructed the lines ourselves and already checked
        // if the lines intersect, this should never happen.
        var determinant = a1 * b2 - a2 * b1;                
        var x = (b2 * c1 - b1 * c2) / determinant; 
        var y = (a1 * c2 - a2 * c1) / determinant; 
        return [x, y];
      }

      /**
       * Makes a "cut" at the datum line from the polygon edge up or down to the polar coordinate.
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
       * @param {string} proj
       */
      var modifyPolarExtent = function(feat, polarCoord, proj) {
        var atNorthPole = polarCoord == ol.proj.fromLonLat(LL_NORTHPOLE, proj);
        var polarLat = atNorthPole ? LL_NORTHPOLE[1] : LL_SOUTHPOLE[1];
        var parts = feat.getGeometry().getCoordinates();

        // Set the point of origin at equator (where datum line towards polar coordinate starts)
        var originCoord = ol.proj.fromLonLat(LL_EQUATORDL, proj);

        if (parts.length > 1) {
          console.warn('Multi-polygon polar search extents are not supported: this might produce unexpected results');
        }

        // Find (1st) polar-side line segment that crosses the "datum".
        // NOTE: this also works for non-square shapes, but NOT for shapes that cross the datum line multiple times!
        var coords = parts[0];
        var intersectionPointIndex = -1;
        var lineStart = null;
        var lineEnd = null;
        for (var i = 0; i < coords.length - 1; i++) {
          var p1 = coords[i];
          var p2 = coords[i + 1];
          if (linesIntersect(p1, p2, originCoord, polarCoord)) {
            intersectionPointIndex = i + 1;
            lineStart = p1;
            lineEnd = p2;
            break;
          }
        }
        
        if (lineStart !== null) {
          // Calculate intersection point between extent segment and datum line
          var intersectionPoint = getIntersection(lineStart, lineEnd, originCoord, polarCoord);
          coords.splice(intersectionPointIndex, 0, intersectionPoint);
          feat.setGeometry(new ol.geom.Polygon([coords]));
        }

        // Transform to WGS 1984 (EPSG:4326)
        feat.getGeometry().transform(proj, LONLAT_WGS84);
        
        if (intersectionPointIndex >= 0) {
          // Get all (transformed) coordinates and previous and next longitudes
          coords = feat.getGeometry().getCoordinates()[0];
          var prevLon = coords[intersectionPointIndex - 1][0];
          var nextLon = coords[intersectionPointIndex + 1][0];
          // Determine polar longitudes (order)
          var polarLon1 = prevLon < 0 && nextLon > 0 ? -180 : 180;
          var polarLon2 = -polarLon1;
          // Get longitude of inserted coordinate, negate if needed (ensure correct hemisphere)
          var insertedLon = coords[intersectionPointIndex][0];
          if ((insertedLon > 0 && polarLon1 < 0) || (insertedLon < 0 && polarLon1 > 0)) {
            insertedLon = -insertedLon;
            coords[intersectionPointIndex][0] = insertedLon;
          }                
          // Get latitude of inserted coordinate
          var insertedLat = coords[intersectionPointIndex][1];
          // Insert 3 coordinates (2 at either side of the pole) to create the "cut"
          coords.splice(intersectionPointIndex + 1, 0, [polarLon1, polarLat], [polarLon2, polarLat], [-insertedLon, insertedLat]);
          feat.setGeometry(new ol.geom.Polygon([coords]));
        }
      }

      /**
       * Transforms the feature geometry into WGS 1984 (EPSG:4326).
       * If the polygon has a custom projection, the coordinates are densified.
       * If the polygon also contains the north or south pole, the geometry is
       * modified so that the resulting polygon will include that pole.
       * @param {ol.feature} feature 
       * @param {string} proj 
       */
      var toLonLat = function(feature, proj) {

        // Input feature already in WGS 1984 (EPSG:4326): no transform needed
        if (proj === LONLAT_WGS84) return feature;

        // Check if the *original* polygon includes a pole
        var polarCoord = getContainedPole(feature, proj);

        // Clone feature so we can manipulate it
        var lonlatFeat = feature.clone();

        if (proj !== WEB_MERCATOR) {
          // Add points to each edge to get a more accurate reprojection
          densifyEdges(lonlatFeat, proj);
        }        

        if (polarCoord) 
        {
          // Original polygon includes a north or south pole: manipulate polygon.
          // Solves issue #4810: Query by south pole area doesn't give any results.
          modifyPolarExtent(lonlatFeat, polarCoord, proj);
        } else {
          // No pole included so a basic transformation to WGS 1984 (EPSG:4326) will do
          lonlatFeat.getGeometry().transform(proj, LONLAT_WGS84);
        }

        return lonlatFeat;
      }

      return {
        toLonLat: toLonLat
      };
    }
  );

})();