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
  goog.provide('gn_graticule');

  var module = angular.module('gn_graticule', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGraticuleBtn
   *
   * @description
   * This directive provides a button to display ol3 graticule on the map.
   * The graticule style is no customisable for the moment.
   */
  module.directive('gnGraticuleBtn', [function() {

    function getTextStyle(degrees, offsetX, offsetY, hemispheres) {
      return new ol.style.Text({
        font: '12px Calibri,sans-serif',
        fill: new ol.style.Fill({
          color: '#000'
        }),
        stroke: new ol.style.Stroke({
          color: '#fff',
          width: 3
        }),
        offsetY: offsetY,
        offsetX: offsetX,
        text: ol.coordinate.degreesToStringHDMS(degrees, hemispheres)
      });
    }

    var segmentSegmentIntersection =
        function(x11, y11, x12, y12, x21, y21, x22, y22) {
      var x11_21 = x11 - x21;
      var y11_21 = y11 - y21;
      var x12_11 = x12 - x11;
      var y12_11 = y12 - y11;
      var x22_21 = x22 - x21;
      var y22_21 = y22 - y21;
      var d = (y22_21 * x12_11) - (x22_21 * y12_11);
      var n1 = (x22_21 * y11_21) - (y22_21 * x11_21);
      var n2 = (x12_11 * y11_21) - (y12_11 * x11_21);
      if (d === 0) {
        // parallel
        if (n1 === 0 && n2 === 0) {
          // coincident
          return [Infinity];
        }
      } else {
        var along1 = n1 / d;
        var along2 = n2 / d;
        if (along1 >= 0 && along1 <= 1 && along2 >= 0 && along2 <= 1) {
          var x = x11 + (along1 * x12_11);
          var y = y11 + (along1 * y12_11);
          return [x, y];
        }
      }
      return [];
    };

    var lineStringSegmentIntersection = function(
        flatCoordinates, offset, end, stride,
        segX1, segY1, segX2, segY2, opt_dest) {
      /** @type {Array.<number>} */
      var intersections = angular.isDefined(opt_dest) ? opt_dest : [];
      var x1 = flatCoordinates[offset];
      var y1 = flatCoordinates[offset + 1];
      for (offset += stride; offset < end; offset += stride) {
        var x2 = flatCoordinates[offset];
        var y2 = flatCoordinates[offset + 1];
        var point = segmentSegmentIntersection(
            x1, y1, x2, y2, segX1, segY1, segX2, segY2);
        if (point.length > 1) {
          var lastX = intersections[intersections.length - 2];
          var lastY = intersections[intersections.length - 1];
          // FIXME ol.geom.SimpleGeometry#equals?
          if (!angular.isDefined(lastX) ||
              lastX != point[0] ||
              lastY != point[1]) {
            Array.prototype.push.apply(intersections, point);
          }
        }
        x1 = x2;
        y1 = y2;
      }
      return intersections;
    };

    ol.coordinate.degreesToStringHDMS = function(degrees, hemispheres) {
      var normalizedDegrees = ((degrees + 180) % 360) - 180;
      var x = Math.abs(Math.round(3600 * normalizedDegrees));
      return Math.floor(x / 3600) + '\u00b0 ' +
          Math.floor((x / 60) % 60) + '\u2032 ' +
          Math.floor(x % 60) + '\u2033 ' +
          hemispheres.charAt(normalizedDegrees < 0 ? 1 : 0);
    };

    var pixel = [];
    var lonLat = [];
    var intersectionPoint = [];


    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/graticule/partials/' +
          'graticule.html',
      scope: true,
      link: function(scope, element, attrs) {
        var graticuleOgcService = scope.$eval(attrs['graticuleOgcService']);
        var graticule = null;

        // for now, only WMS services are supported by the directive
        if (graticuleOgcService) {
          if (!graticuleOgcService.layer || !graticuleOgcService.url) {
            console.error('Missing property for graticule layer ' +
                '(required: \'url\', \'layer\'):\n', graticuleOgcService);
            return;
          }

          graticule = new ol.layer.Image({
            source: new ol.source.ImageWMS({
              url: graticuleOgcService.url,
              params: { 'LAYERS': graticuleOgcService.layer },
              ratio: 1
            })
          });
          graticule.background = true;  // do not save it in context
          graticule.setZIndex(1000);    // TODO: uncomment after OL upgrade
          graticule.setVisible(false);  // hidden by default

          // 'active' prop makes ogc layer visible/invisible
          Object.defineProperty(graticule, 'active', {
            get: function() {
              return graticule.getVisible();
            },
            set: function(val) {
              // add layer to map if not already done
              var map = scope.$eval(attrs['gnGraticuleBtn']);
              if (map.getLayers().getArray().indexOf(graticule) === -1) {
                map.addLayer(graticule);
              }
              graticule.setVisible(val);
            }
          });
        } else {
          graticule = new ol.Graticule({
            strokeStyle: new ol.style.Stroke({
              color: 'rgba(255,120,0,0.9)',
              width: 1,
              lineDash: [0.5, 1]
            })
          });

          // 'active' prop adds or remove ol.Graticule object
          Object.defineProperty(graticule, 'active', {
            get: function() {
              return !!graticule.getMap();
            },
            set: function(val) {
              if (val) {
                graticule.setMap(map);
                map.on('postcompose', renderCoords);

              } else {
                graticule.setMap(null);
                map.un('postcompose', renderCoords);
              }
            }
          });
        }
        scope.graticule = graticule;

        var map = scope.$eval(attrs['gnGraticuleBtn']);
        var transform = ol.proj.getTransform(map.getView().getProjection(),
            'EPSG:4326');

        var renderCoords = function(e) {
          var frameState = e.frameState;
          var mapSize = frameState.size;
          var vectorContext = e.vectorContext;

          pixel[0] = 0;
          pixel[1] = 0;
          var topLeft = map.getCoordinateFromPixel(pixel);
          pixel[0] = mapSize[0];
          pixel[1] = 0;
          var topRight = map.getCoordinateFromPixel(pixel);
          pixel[0] = mapSize[0];
          pixel[1] = mapSize[1];
          var bottomRight = map.getCoordinateFromPixel(pixel);

          var i, l, flatCoordinates, coords, point, textStyle, stride;
          var lineString, lineStrings;

          lineStrings = graticule.getMeridians();
          for (i = 0, l = lineStrings.length; i < l; ++i) {
            lineString = lineStrings[i];
            coords = lineString.getCoordinates();
            flatCoordinates = [coords[0][0], coords[0][1],
              coords[1][0], coords[1][1]];

            intersectionPoint.length = 0;
            lineStringSegmentIntersection(
                flatCoordinates, 0, flatCoordinates.length, coords[0].length,
                topLeft[0], topLeft[1], topRight[0], topRight[1],
                intersectionPoint);
            if (intersectionPoint.length <= 0) {
              ol.geom.flat.interpolate.lineString(
                  flatCoordinates, 0, flatCoordinates.length, coords[0].length,
                  1, intersectionPoint);
            }
            point = new ol.geom.Point(null);
            point.setCoordinates(intersectionPoint);
            transform(intersectionPoint, lonLat);
            textStyle = getTextStyle(lonLat[0], 0, 10, 'EW');
            vectorContext.setTextStyle(textStyle);
            vectorContext.drawGeometry(point);
            vectorContext.setTextStyle(null);

          }

          lineStrings = graticule.getParallels();
          for (i = 0, l = lineStrings.length; i < l; ++i) {
            lineString = lineStrings[i];
            coords = lineString.getCoordinates();
            flatCoordinates = [coords[0][0], coords[0][1],
              coords[1][0], coords[1][1]];
            intersectionPoint.length = 0;
            lineStringSegmentIntersection(
                flatCoordinates, 0, flatCoordinates.length, coords[0].length,
                topRight[0], topRight[1], bottomRight[0], bottomRight[1],
                intersectionPoint);
            if (intersectionPoint.length <= 0) {
              intersectionPoint = ol.geom.flat.interpolate.lineString(
                  flatCoordinates, 0, flatCoordinates.length, coords[0].length,
                  1, intersectionPoint);
            }
            point = new ol.geom.Point(null);
            point.setCoordinates(intersectionPoint);
            transform(intersectionPoint, lonLat);
            textStyle = getTextStyle(lonLat[1], -30, 0, 'NS');
            vectorContext.setTextStyle(textStyle);
            vectorContext.drawGeometry(point, null);
            vectorContext.setTextStyle(null);

          }
        };

      }


    };
  }]);
})();
