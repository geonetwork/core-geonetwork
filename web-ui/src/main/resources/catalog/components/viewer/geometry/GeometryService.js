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
  goog.provide("gn_geometry_service");

  var module = angular.module("gn_geometry_service", []);

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
  module.service("gnGeometryService", [
    function () {
      var LONLAT_WGS84 = "EPSG:4326"; // Projection code of WGS 1984 coordinate system
      var WEB_MERCATOR = "EPSG:3857"; // Projection code of (Google) Web Mercator
      var LL_SOUTHPOLE = [0, -90]; // South pole longitude-latitude coordinate
      var LL_NORTHPOLE = [0, 90]; // North pole longitude-latitude coordinate
      var LL_EQUATORDL = [180, 0]; // Longitude-latitude coordinate of datum line at equator

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
      this.getFormatFromMimeType = function (mimeType) {
        var parts = mimeType.split(";");
        parts.forEach(function (p) {
          p = p.trim().toLowerCase();
        });

        switch (parts[0]) {
          case "application/vnd.geo+json":
          case "application/json":
            return "geojson";

          case "application/wkt":
            return "wkt";

          case "application/gml+xml":
          case "text/xml":
            return "gml";

          default:
            return "object";
        }
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#getCommonLayer
       *
       * @description
       * This fetches a OL vector layer that is common to all Geometry Tools
       * If non existent, the layer will be created
       *
       * @param {ol.Map} map open layers map
       * @param {!string} name suffix (point, polygon, lines) optional
       * @return {ol.layer.Vector} vector layer
       */
      this.getCommonLayer = function (map, name) {
        name
          ? (name = "geometry-tool-layer" + "-" + name)
          : (name = "geometry-tool-layer");
        var commonLayer = null;
        map
          .getLayers()
          .getArray()
          .forEach(function (layer) {
            if (layer.get("name") === name) {
              commonLayer = layer;
            }
          });

        if (commonLayer) {
          return commonLayer;
        }

        // layer & source
        var source = new ol.source.Vector({
          wrapX: false,
          useSpatialIndex: true,
          features: new ol.Collection()
        });
        commonLayer = new ol.layer.Vector({
          source: source,
          name: name,
          style: [
            new ol.style.Style({
              // this is the default editing style
              fill: new ol.style.Fill({
                color: "rgba(255, 255, 255, 0.5)"
              }),
              stroke: new ol.style.Stroke({
                color: "white",
                width: 5
              })
            }),
            new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: "rgba(0, 153, 255, 1)",
                width: 3
              }),
              image: new ol.style.Circle({
                radius: 6,
                fill: new ol.style.Fill({
                  color: "rgba(0, 153, 255, 1)"
                }),
                stroke: new ol.style.Stroke({
                  color: "white",
                  width: 1.5
                })
              })
            })
          ]
        });
        commonLayer.setZIndex(100);

        // add our layer to the map
        map.get("creationPromise").then(function () {
          map.addLayer(commonLayer);
        });

        return commonLayer;
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#printGeometryOutput
       *
       * @description
       * This prints the output of a geometry tool according to options given
       *
       * @param {ol.Map} map open layers map
       * @param {ol.Feature} feature feature to output (can be multi)
       * @param {object} options options
       * @param {string} options.crs default is EPSG:4326
       * @param {string} options.format default is GML
       * @param {bool} options.outputAsWFSFeaturesCollection default is false
       * @param {bool} options.outputAsFeatures default is false
       * @param {string} options.gmlFeatureElement feature element name
       * @param {string} options.gmlFeatureNS feature element namespace
       * @return {string | ol.geom.Geometry | Array.<ol.Feature>} output
       *  as string or object
       */
      this.printGeometryOutput = function (map, feature, options) {
        var options = angular.extend(
          {
            crs: LONLAT_WGS84,
            format: "gml"
          },
          options
        );

        // clone & transform geom
        var outputGeom = feature
          .getGeometry()
          .clone()
          .transform(map.getView().getProjection(), options.crs || LONLAT_WGS84);
        var outputFeature = new ol.Feature({
          geometry: outputGeom
        });

        // set id on feature
        feature.setId("geometry-tool-output");

        var formatLabel = options.format.toLowerCase();
        var format;
        var outputValue;
        switch (formatLabel) {
          case "json":
          case "geojson":
            format = new ol.format.GeoJSON();
            if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              outputValue = format.writeGeometry(outputGeom);
            }
            break;

          case "wkt":
            format = new ol.format.WKT();
            if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              outputValue = format.writeGeometry(outputGeom);
            }
            break;

          case "gml":
            format = new ol.format.GML({
              featureNS: options.gmlFeatureNS || "http://mapserver.gis.umn.edu/mapserver",
              featureType: options.gmlFeatureElement || "features",
              srsName: options.crs
            });

            if (options.outputAsWFSFeaturesCollection) {
              outputValue =
                "<wfs:FeatureCollection " +
                'xmlns:wfs="http://www.opengis.net/wfs">' +
                format.writeFeatures([outputFeature]) +
                "</wfs:FeatureCollection>";
            } else if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              var nodes = format.writeFeaturesNode([outputFeature]).firstChild.childNodes;
              var geom = null;
              for (var i = 0; i < nodes.length; i++) {
                var node = nodes.item(0);
                if (node.localName === outputFeature.getGeometryName()) {
                  geom = node;
                }
              }
              if (!geom) {
                console.warn("No geometry found for feature", feature);
                return null;
              }
              outputValue = geom.innerHTML;
            }
            break;

          // no valid format specified: output as object + give warning
          default:
            console.warn(
              "No valid output format specified for " +
                "gn-geometry-tool (value=" +
                options.format +
                "); " +
                "outputting geometry as object"
            );

          case "object":
            if (options.outputAsFeatures) {
              outputValue = [outputFeature];
            } else {
              outputValue = outputGeom;
            }
            break;
        }

        return outputValue;
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:appendToMultiGeometry
       * @name gnGeometryService#appendToMultiGeometry
       *
       * @description
       * Takes a multi geometry and an array of geometries (of length 1) in order
       * to merge the first element of the array into the base geometry
       *
       * @param {ol.geom} baseGeom # multi
       * @param {array<ol.geom>} multiGeometries # multi
       * @return {ol.geom} merged geometries
       */
      this.appendToMultiGeometry = function (baseGeom, multiGeometries) {
        var geomType = baseGeom.getType();
        switch (geomType) {
          case "MultiPoint":
            multiGeometries.forEach(function (f) {
              baseGeom.appendPoint(f.getGeometry().getPoints()[0]);
            });
            break;
          case "MultiLineString":
            multiGeometries.forEach(function (f) {
              baseGeom.appendLineString(f.getGeometry().getLineStrings()[0]);
            });
            break;
          case "MultiPolygon":
            multiGeometries.forEach(function (f) {
              baseGeom.appendPolygon(f.getGeometry().getPolygons()[0]);
            });
            break;
          default:
            console.error(
              "Error when getting geometry type '{}' is not supported".format(geomType)
            );
            break;
        }
        return baseGeom;
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#printGeometryOutput
       *
       * @description
       * This parses a text to create an Open Layers geometry object.
       *
       * @param {ol.Map} map open layers map
       * @param {string} input as text
       * @param {object} options options
       * @param {string} options.crs default is EPSG:4326
       * @param {string} options.format default is GML
       * @param {string} options.gmlFeatureElement feature element name
       * @param {string} options.gmlFeatureNS feature element ns
       * @return {ol.geom.Geometry} geometry object
       */
      this.parseGeometryInput = function (map, input, options) {
        var options = angular.extend(
          {
            crs: LONLAT_WGS84,
            format: "gml"
          },
          options
        );

        var formatLabel = options.format.toLowerCase();
        var format;
        var outputProjection = map.getView().getProjection();
        var outputValue = null;
        switch (formatLabel) {
          case "json":
          case "geojson":
            format = new ol.format.GeoJSON();
            outputValue = format.readGeometry(input, {
              dataProjection: options.crs,
              featureProjection: outputProjection
            });
            break;

          case "wkt":
            format = new ol.format.WKT();
            outputValue = format.readGeometry(input, {
              dataProjection: options.crs,
              featureProjection: outputProjection
            });
            break;

          case "gml":
            format = new ol.format.GML({
              featureNS: "http://www.opengis.net/gml",
              featureType: "feature"
            });
            var fullXml =
              "<featureMembers>" +
              '<gml:feature xmlns:gml="http://www.opengis.net/gml">' +
              "<geometry>" +
              input +
              "</geometry></gml:feature></featureMembers>";

            var feature = format.readFeatures(
              fullXml.replace(
                /http:\/\/www\.opengis\.net\/gml\/3.2/g,
                "http://www.opengis.net/gml"
              ),
              {
                dataProjection: options.crs,
                featureProjection: outputProjection
              }
            )[0];
            outputValue = fixGmlGeometryLayout(feature.getGeometry(), input);
            break;

          // no valid format specified: handle as object
          default:
          case "object":
            if (!input instanceof ol.geom.Geometry) {
              console.error(
                "gn-geometry-tool input was supposed to be a " +
                  "ol.geom.Geometry object but was something else, " +
                  "skipping parse.",
                input
              );
              return outputValue;
            }
            outputValue = input.clone.transform(options.crs, outputProjection);
            break;
        }

        return outputValue;
      };

      /**
       * Forces geometry that has been created from GML to become 2D
       * if the srsDimension attribute in the GML equals 2.
       * OpenLayers GML parser always outputs 3D feature members.
       * If the srsDimension attribute is 3 (Z) or 4 (ZM), no fix is applied.
       * NOTE: the fix is applied in-place.
       *
       * @param {ol.geom.Geometry} geom geometry to check and fix
       * @param {string} gmlString original GML used to create the geometry
       * @returns {ol.geom.Geometry} a (possibly) modified geometry
       */
      var fixGmlGeometryLayout = function (geom, gmlString) {
        // Get dimensions from GML
        var dim = gmlString.match(new RegExp('srsDimension="([^"]*)"'));
        // If srsDimension > 2 OR geometry is already 2D, return unmodified geometry
        if (
          (dim && dim.length === 2 && dim[1] >= 3) ||
          (geom.layout === "XY" && geom.stride === 2)
        )
          return geom;
        // Drop Z (and M)
        geom.setCoordinates(geom.getCoordinates(), "XY");
        return geom;
      };

      /**
       * Checks if the feature geometry contains the north or south pole.
       * Returns the polar coordinate in the current map projection when found or null otherwise.
       * NOTE: Will not trigger if pole is on the edge.
       *
       * @param {ol.feature} feat feature for which to check its geometry
       * @param {string} proj current feature geometry projection
       * @returns {null | Array.<number>} polar coordinate
       */
      var getContainedPole = function (feat, proj) {
        var geom = feat.getGeometry();
        var southPole = ol.proj.fromLonLat(LL_SOUTHPOLE, proj);
        var northPole = ol.proj.fromLonLat(LL_NORTHPOLE, proj);
        if (geom.intersectsCoordinate(southPole)) {
          console.debug("Geometry contains south pole");
          return southPole;
        } else if (geom.intersectsCoordinate(northPole)) {
          console.debug("Geometry contains north pole");
          return northPole;
        }
      };

      /**
       * Transforms the feature geometry in-place to longitude-latitude coordinates (EPSG:4326).
       *
       * @param {ol.feature} feat feature to transform (in-place)
       * @param {string} inputProj current feature geometry projection
       */
      var transformGeometry = function (feat, inputProj) {
        feat.getGeometry().transform(inputProj, LONLAT_WGS84);
      };

      /**
       * Breaks each segment of an extent polygon into 4 parts in-place.
       * The number of points that will be added to each segment is 3.
       * If the polygon has more than 5 coordinates, the function
       * assumes that it's not an extent and will not densify.
       * NOTE: if the geometry is not extent-like, it remains the same.
       *
       * @param {ol.feature} feat feature to densify (in-place)
       */
      var densifyEdges = function (feat) {
        var outCoords = [];
        // Get first/outer ring
        var geom = feat.getGeometry();
        var inCoords = geom.getCoordinates()[0];
        // Do not densify if shape does not appear to be an extent
        if (geom.getType() !== "Polygon" || inCoords.length > 5) return;
        for (var i = 0; i < inCoords.length - 1; i++) {
          var p0, p1, x0, y0, dX;
          p0 = inCoords[i];
          p1 = inCoords[i + 1];
          x0 = p0[0];
          y0 = p0[1];
          dX = (p1[0] - x0) / 4;
          dY = (p1[1] - y0) / 4;
          outCoords.push(p0);
          for (var j = 1; j < 4; j++) {
            // Add 3 coords
            outCoords.push([x0 + j * dX, y0 + j * dY]);
          }
        }
        // Add first coord to close polygon
        outCoords.push(inCoords[0]);
        feat.setGeometry(new ol.geom.Polygon([outCoords]));
      };

      /**
       * Given three colinear points (p1, p2, p3), check if p2 lies on line segment p1-p3.
       *
       * @param {Array.<number>} p1 point 1
       * @param {Array.<number>} p2 point 2
       * @param {Array.<number>} p3 point 3
       * @returns {bool} true if on segment
       */
      var onSegment = function (p1, p2, p3) {
        if (
          p2[0] <= Math.max(p1[0], p3[0]) &&
          p2[0] >= Math.min(p1[0], p3[0]) &&
          p2[1] <= Math.max(p1[1], p3[1]) &&
          p2[1] >= Math.min(p1[1], p3[1])
        ) {
          return true;
        }
        return false;
      };

      /**
       * Finds the orientation of an ordered triplet (p1, p2, p3).
       * Returns one of the following values:
       * 0 : Colinear points
       * 1 : Clockwise points
       * 2 : Counterclockwise
       *
       * See https://www.geeksforgeeks.org/orientation-3-ordered-points/amp for details of below formula.
       *
       * @param {Array.<number>} p1 point 1
       * @param {Array.<number>} p2 point 2
       * @param {Array.<number>} p3 point 3
       * @returns {int} 0, 1 or 2
       */
      var getOrientation = function (p1, p2, p3) {
        var value = (p2[1] - p1[1]) * (p3[0] - p2[0]) - (p2[0] - p1[0]) * (p3[1] - p2[1]);
        if (value > 0) return 1;
        else if (value < 0) return 2;
        return 0;
      };

      /**
       * Returns true if the line segments 'line1' and 'line2' (with points A and B) intersect.
       * @param {Array.<number>} line1A point A of first line
       * @param {Array.<number>} line1B point B of first line
       * @param {Array.<number>} line2A point A of second line
       * @param {Array.<number>} line2B point B of second line
       * @returns {bool} true if lines intersect
       */
      var linesIntersect = function (line1A, line1B, line2A, line2B) {
        // Find the 4 orientations required for the general and special cases
        var o1 = getOrientation(line1A, line1B, line2A);
        var o2 = getOrientation(line1A, line1B, line2B);
        var o3 = getOrientation(line2A, line2B, line1A);
        var o4 = getOrientation(line2A, line2B, line1B);

        // General case
        if (o1 !== o2 && o3 !== o4) return true;

        // Special cases:
        // Line1(A,B) and Line2(A) are colinear and Line2(A) lies on Line1
        if (o1 === 0 && onSegment(line1A, line2A, line1B)) return true;
        // Line1(A,B) and Line2(B) are colinear and Line2(B) lies on Line1
        if (o2 === 0 && onSegment(line1A, line2B, line1B)) return true;
        // Line2(A,B) and Line1(A) are colinear and Line1(A) lies on Line2
        if (o3 === 0 && onSegment(line2A, line1A, line2B)) return true;
        // Line2(A,B) and Line1(B) are colinear and Line1(B) lies on Line2
        if (o4 === 0 && onSegment(line2A, line1B, line2B)) return true;
        // Nothing intersects
        return false;
      };

      /**
       * Calculates the intersection point between 'line1' and 'line2'.
       * Assumes that the lines actually intersect (use linesIntersect() to verify).
       * @param {Array.<number>} line1A point A of first line
       * @param {Array.<number>} line1B point B of first line
       * @param {Array.<number>} line2A point A of second line
       * @param {Array.<number>} line2B point B of second line
       * @returns {Array.<number>} intersection coordinate
       */
      var getIntersection = function (line1A, line1B, line2A, line2B) {
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
      };

      /**
       * Makes a "cut" at the datum line from the polygon edge up or down to the polar coordinate.
       * The feature geometry is also transformed to WGS 1984 (EPSG:4326) in-place.
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
       * @param {ol.feature} feat input feature to modify (in-place)
       * @param {Array.<number>} polarCoord contained polar coordinate
       * @param {string} proj input feature geometry projection
       */
      var modifyPolarPolygon = function (feat, polarCoord, proj) {
        var atNorthPole = polarCoord == ol.proj.fromLonLat(LL_NORTHPOLE, proj);
        var polarLat = atNorthPole ? LL_NORTHPOLE[1] : LL_SOUTHPOLE[1];
        var geom = feat.getGeometry();

        // Do not modify if geometry is not a polygon: just transform to WGS 1984 (EPSG:4326)
        if (geom.getType() !== "Polygon") {
          return transformGeometry(feat, proj);
        }

        // Set the point of origin at equator (where datum line towards polar coordinate starts)
        var originCoord = ol.proj.fromLonLat(LL_EQUATORDL, proj);

        // Find (1st) polar-side line segment that crosses the "datum".
        // NOTE: this also works for non-square shapes, but NOT for shapes that cross the datum line multiple times!
        var coords = geom.getCoordinates()[0];
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
          var intersectionPoint = getIntersection(
            lineStart,
            lineEnd,
            originCoord,
            polarCoord
          );
          coords.splice(intersectionPointIndex, 0, intersectionPoint);
          feat.setGeometry(new ol.geom.Polygon([coords]));
        }

        // Transform to WGS 1984 (EPSG:4326)
        transformGeometry(feat, proj);

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
          coords.splice(
            intersectionPointIndex + 1,
            0,
            [polarLon1, polarLat],
            [polarLon2, polarLat],
            [-insertedLon, insertedLat]
          );
          feat.setGeometry(new ol.geom.Polygon([coords]));
        }
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#featureToLonLat
       *
       * @description
       * Transforms the feature geometry into WGS 1984 (EPSG:4326).
       * If the polygon has a custom projection, the coordinates are densified.
       * If the polygon also contains the north or south pole, the geometry is
       * modified so that the resulting polygon will include that pole.
       * NOTE: If the geometry is not a polygon, it will be transformed as-is.
       * If the projection already is EPSG:4326, the original feature is returned.
       * In all other cases, a feature clone is returned.
       *
       * @param {ol.feature} feature input feature to transform
       * @param {string} proj current feature geometry projection
       * @returns {ol.feature} output feature
       */
      this.featureToLonLat = function (feature, proj) {
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

        if (polarCoord) {
          // Original polygon includes a north or south pole: manipulate polygon.
          // Solves issue #4810: Query by south pole area doesn't give any results.
          modifyPolarPolygon(lonlatFeat, polarCoord, proj);
        } else {
          // No pole included so a basic transformation to WGS 1984 (EPSG:4326) will do
          transformGeometry(lonlatFeat, proj);
        }

        return lonlatFeat;
      };
    }
  ]);
})();
