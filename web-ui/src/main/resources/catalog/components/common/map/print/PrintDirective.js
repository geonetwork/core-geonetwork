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
  goog.provide("ga_print_directive");

  // Source from https://github.com/geoadmin/mf-geoadmin3
  var module = angular.module("ga_print_directive", ["pascalprecht.translate"]);

  module.controller("GaPrintDirectiveController", [
    "$scope",
    "$http",
    "$window",
    "$translate",
    "$document",
    function ($scope, $http, $window, $translate, $document) {
      var waitclass = "ga-print-wait";
      var bodyEl = angular.element($document[0].body);
      bodyEl.removeClass(waitclass);
      var pdfLegendsToDownload = [];
      var pdfLegendString = "_big.pdf";
      var printRectangle;
      var deregister;
      var DPI = 72;
      var DPI2 = 254;
      var MM_PER_INCHES = 25.4;
      var UNITS_RATIO = 39.37;
      $scope.options = {
        printConfigUrl: "../../pdf/info.json?url=..%2F..%2Fpdf",
        heightMargin: 0,
        widthMargin: 0
      };

      /**
       * Unmanaged layer with custom render method
       * used to draw the grey rectangle for print extent
       */
      var overlayCanvas = document.createElement("canvas");
      overlayCanvas.style.position = "absolute";
      overlayCanvas.style.width = "100%";
      overlayCanvas.style.height = "100%";
      var overlayLayer = new ol.layer.Layer({
        render: function () {
          // print rectangle might not be ready if config is loading
          if (!printRectangle) {
            return;
          }

          var size = $scope.map.getSize();
          var height = size[1] * ol.has.DEVICE_PIXEL_RATIO;
          var width = size[0] * ol.has.DEVICE_PIXEL_RATIO;
          overlayCanvas.width = width;
          overlayCanvas.height = height;
          var ctx = overlayCanvas.getContext("2d");

          var minx, miny, maxx, maxy;
          (minx = printRectangle[0]),
            (miny = printRectangle[1]),
            (maxx = printRectangle[2]),
            (maxy = printRectangle[3]);

          ctx.beginPath();
          // Outside polygon, must be clockwise
          ctx.moveTo(0, 0);
          ctx.lineTo(width, 0);
          ctx.lineTo(width, height);
          ctx.lineTo(0, height);
          ctx.lineTo(0, 0);
          ctx.closePath();

          // Inner polygon,must be counter-clockwise
          ctx.moveTo(minx, miny);
          ctx.lineTo(minx, maxy);
          ctx.lineTo(maxx, maxy);
          ctx.lineTo(maxx, miny);
          ctx.lineTo(minx, miny);
          ctx.closePath();

          ctx.fillStyle = "rgba(0, 5, 25, 0.75)";
          ctx.fill();

          return overlayCanvas;
        }
      });

      // Get print config
      var updatePrintConfig = function () {
        var http = $http.get($scope.options.printConfigUrl);
        http.then(function (response) {
          var data = response.data;

          $scope.capabilities = data;

          // default values:
          $scope.layout = data.layouts[0];
          if ($scope.defaultLayout) {
            angular.forEach(data.layouts, function (layout) {
              if (layout.name === $scope.defaultLayout) {
                $scope.layout = layout;
              }
            });
          }
          $scope.dpi = data.dpis[0];
          $scope.scales = data.scales;
          $scope.scale = data.scales[5];
          $scope.options.legend = false;
          $scope.options.graticule = false;
        });
        return http;
      };

      var activate = function () {
        updatePrintConfig().then(function () {
          deregister = [
            $scope.map.getView().on("propertychange", function (event) {
              updatePrintRectanglePixels($scope.scale);
            })
          ];
          $scope.scale = getOptimalScale();
          refreshComp();
          registerEvents();
          overlayLayer.setMap($scope.map);
        });
      };

      var deactivate = function () {
        if (deregister) {
          for (var i = 0; i < deregister.length; i++) {
            ol.Observable.unByKey(deregister[i]);
          }
        }
        overlayLayer.setMap(null);
        refreshComp();
      };

      var refreshComp = function () {
        updatePrintRectanglePixels($scope.scale);
        if ($scope.map) {
          $scope.map.render();
        }
      };

      // Listeners
      var registerEvents = function () {
        //      $scope.$on('gaLayersChange', onMoveEnd);
        //      $scope.map.on('change:size', onMoveEnd);
        $scope.$watch("scale", function () {
          updatePrintRectanglePixels($scope.scale);
          $scope.getConfig();
        });
        $scope.$watch("layout", function () {
          updatePrintRectanglePixels($scope.scale);
          $scope.getConfig();
        });
        $scope.map.on("moveend", function () {
          $scope.$apply(function () {
            $scope.getConfig();
          });
        });
      };

      // Encode ol.Layer to a basic js object
      var encodeLayer = function (layer, proj) {
        var encLayer, encLegend;
        var ext = proj.getExtent();
        var resolution = $scope.map.getView().getResolution();

        if (!(layer instanceof ol.layer.Group)) {
          var src = layer.getSource();
          var layerConfig = {}; // gaLayers.getLayer(layer.bodId) ||
          var minResolution = layerConfig.minResolution || 0;
          var maxResolution = layerConfig.maxResolution || Infinity;

          if (resolution <= maxResolution && resolution >= minResolution) {
            if (src instanceof ol.source.WMTS) {
              encLayer = $scope.encoders.layers["WMTS"].call(
                this,
                layer,
                layerConfig,
                proj
              );
            } else if (src instanceof ol.source.OSM) {
              encLayer = $scope.encoders.layers["OSM"].call(this, layer, layerConfig);
            } else if (
              src instanceof ol.source.ImageWMS ||
              src instanceof ol.source.TileWMS
            ) {
              encLayer = $scope.encoders.layers["WMS"].call(
                this,
                layer,
                layerConfig,
                proj
              );
            } else if (layer instanceof ol.layer.Vector) {
              var features = [];
              src.forEachFeatureInExtent(ext, function (feat) {
                features.push(feat);
              });

              if (features && features.length > 0) {
                encLayer = $scope.encoders.layers["Vector"].call(this, layer, features);
              }
            }
          }
        }

        if ($scope.options.legend && layerConfig.hasLegend) {
          encLegend = $scope.encoders.legends["ga_urllegend"].call(
            this,
            layer,
            layerConfig
          );

          if (encLegend.classes && encLegend.classes[0] && encLegend.classes[0].icon) {
            var legStr = encLegend.classes[0].icon;
            if (
              legStr.indexOf(pdfLegendString, legStr.length - pdfLegendString.length) !==
              -1
            ) {
              pdfLegendsToDownload.push(legStr);
              encLegend = undefined;
            }
          }
        }
        return { layer: encLayer, legend: encLegend };
      };

      // Create a ol.geom.Polygon from an ol.geom.Circle, comes from OL2
      // https://github.com/openlayers/openlayers/blob/master/lib/OpenLayers/Geometry/Polygon.js#L240
      var circleToPolygon = function (circle, sides, rotation) {
        var origin = circle.getCenter();
        var radius = circle.getRadius();
        sides = sides || 40;
        var angle = Math.PI * (1 / sides - 1 / 2);
        if (rotation) {
          angle += (rotation / 180) * Math.PI;
        }
        var points = [];
        for (var i = 0; i < sides; ++i) {
          var rotatedAngle = angle + (i * 2 * Math.PI) / sides;
          var x = origin[0] + radius * Math.cos(rotatedAngle);
          var y = origin[1] + radius * Math.sin(rotatedAngle);
          points.push([x, y]);
        }
        points.push(points[0]); // Close the polygon
        return new ol.geom.Polygon([points]);
      };

      // Transform an ol.Color to an hexadecimal string
      var toHexa = function (olColor) {
        var hex = "#";
        for (var i = 0; i < 3; i++) {
          var part = olColor[i].toString(16);
          if (part.length === 1 && parseInt(part) < 10) {
            hex += "0";
          }
          hex += part;
        }
        return hex;
      };

      // Transform a ol.style.Style to a print literal object
      var transformToPrintLiteral = function (feature, style) {
        /**
         * ol.style.Style properties:
         *
         *  fill: ol.style.Fill :
         *    fill: String
         *  image: ol.style.Image:
         *    anchor: array[2]
         *    rotation
         *    size: array[2]
         *    src: String
         *  stroke: ol.style.Stroke:
         *    color: String
         *    lineCap
         *    lineDash
         *    lineJoin
         *    miterLimit
         *    width: Number
         *  text
         *  zIndex
         */

        /**
         * Print server properties:
         *
         * fillColor
         * fillOpacity
         * strokeColor
         * strokeOpacity
         * strokeWidth
         * strokeLinecap
         * strokeLinejoin
         * strokeDashstyle
         * pointRadius
         * label
         * fontFamily
         * fontSize
         * fontWeight
         * fontColor
         * labelAlign
         * labelOutlineColor
         * labelOutlineWidth
         * graphicHeight
         * graphicOpacity
         * graphicWidth
         * graphicXOffset
         * graphicYOffset
         * zIndex
         */

        var literal = {
          zIndex: style.getZIndex()
        };
        var type = feature.getGeometry().getType();
        var fill = style.getFill();
        var stroke = style.getStroke();
        var textStyle = style.getText();
        var imageStyle = style.getImage();

        if (imageStyle) {
          var size = imageStyle.getSize();
          var anchor = imageStyle.getAnchor();
          var scale = imageStyle.getScale();
          literal.rotation = imageStyle.getRotation();
          if (size) {
            literal.graphicWidth = size[0] * scale;
            literal.graphicHeight = size[1] * scale;
          }
          if (anchor) {
            literal.graphicXOffset = -anchor[0] * scale;
            literal.graphicYOffset = -anchor[1] * scale;
          }
          if (imageStyle instanceof ol.style.Icon) {
            literal.externalGraphic = imageStyle.getSrc();
          } else {
            // ol.style.Circle
            fill = imageStyle.getFill();
            stroke = imageStyle.getStroke();
            literal.pointRadius = imageStyle.getRadius();
          }
        }

        if (fill) {
          var color = ol.color.asArray(fill.getColor());
          literal.fillColor = toHexa(color);
          literal.fillOpacity = color[3];
        } else {
          literal.fillOpacity = 0; // No fill
        }

        if (stroke) {
          var color = ol.color.asArray(stroke.getColor());
          literal.strokeWidth = stroke.getWidth();
          literal.strokeColor = toHexa(color);
          literal.strokeOpacity = color[3];
          literal.strokeLinecap = stroke.getLineCap() || "round";
          literal.strokeLinejoin = stroke.getLineJoin() || "round";

          if (stroke.getLineDash()) {
            literal.strokeDashstyle = "dash";
          }
          // TO FIX: Not managed by the print server
          // literal.strokeMiterlimit = stroke.getMiterLimit();
        } else {
          literal.strokeOpacity = 0; // No Stroke
        }

        if (textStyle) {
          var fillColor = ol.color.asArray(textStyle.getFill().getColor());
          var strokeColor = ol.color.asArray(textStyle.getStroke().getColor());
          var fontValues = textStyle.getFont().split(" ");
          literal.fontColor = toHexa(fillColor);
          // Fonts managed by print server: COURIER, HELVETICA, TIMES_ROMAN
          literal.fontFamily = fontValues[2].toUpperCase();
          literal.fontSize = parseInt(fontValues[1]);
          literal.fontWeight = fontValues[0];
          literal.label = textStyle.getText();
          literal.labelAlign = textStyle.getTextAlign();
          literal.labelOutlineColor = toHexa(strokeColor);
          literal.labelOutlineWidth = textStyle.getStroke().getWidth();
        }

        return literal;
      };

      // Encoders by type of layer
      $scope.encoders = {
        layers: {
          Layer: function (layer) {
            var enc = {
              layer: layer.bodId,
              opacity: layer.getOpacity()
            };
            return enc;
          },
          Group: function (layer, proj) {
            var encs = [];
            var subLayers = layer.getLayers();
            subLayers.forEach(function (subLayer, idx, arr) {
              if (subLayer.visible) {
                var enc = $scope.encoders.layers["Layer"].call(this, layer);
                var layerEnc = encodeLayer(subLayer, proj);
                if (layerEnc && layerEnc.layer) {
                  $.extend(enc, layerEnc);
                  encs.push(enc.layer);
                }
              }
            });
            return encs;
          },
          Vector: function (layer, features) {
            var enc = $scope.encoders.layers["Layer"].call(this, layer);
            var format = new ol.format.GeoJSON();
            var encStyles = {};
            var encFeatures = [];
            var stylesDict = {};
            var styleId = 0;

            angular.forEach(features, function (feature) {
              var encStyle = {
                id: styleId
              };
              var styles = layer.getStyleFunction()
                ? layer.getStyleFunction()(feature)
                : ol.feature.defaultStyleFunction(feature);

              var geometry = feature.getGeometry();

              // Transform an ol.geom.Circle to a ol.geom.Polygon
              if (geometry.getType() === "Circle") {
                var polygon = circleToPolygon(geometry);
                feature = new ol.Feature(polygon);
              }

              var encJSON = format.writeFeature(feature);
              if (!encJSON.properties) {
                encJSON.properties = {};

                // Fix https://github.com/geoadmin/mf-geoadmin3/issues/1213
              } else if (encJSON.properties.Style) {
                delete encJSON.properties.Style;
              }

              encJSON.properties._gx_style = styleId;
              encFeatures.push(encJSON);

              if (styles && styles.length > 0) {
                $.extend(encStyle, transformToPrintLiteral(feature, styles[0]));
              }

              encStyles[styleId] = encStyle;
              styleId++;
            });
            angular.extend(enc, {
              type: "Vector",
              styles: encStyles,
              styleProperty: "_gx_style",
              geoJson: {
                type: "FeatureCollection",
                features: encFeatures
              },
              name: layer.bodId,
              opacity: layer.opacity != null ? layer.opacity : 1.0
            });
            return enc;
          },
          WMS: function (layer, config, proj) {
            var enc = $scope.encoders.layers["Layer"].call(this, layer);
            var params = layer.getSource().getParams();
            var layers = params.LAYERS.split(",") || [];
            var styles =
              params.STYLES !== undefined
                ? params.STYLES.split(",")
                : new Array(layers.length).join(",").split(",");

            angular.extend(enc, {
              type: "WMS",
              baseURL:
                config.wmsUrl ||
                layer.url ||
                layer.getSource().getParams().URL ||
                layer.getSource().getUrls()[0],
              layers: layers,
              styles: styles,
              format: "image/" + (config.format || "png"),
              customParams: {
                EXCEPTIONS: "XML",
                TRANSPARENT: "true",
                CRS: proj.getCode(),
                TIME: params.TIME
              },
              singleTile: config.singleTile || true
            });
            return enc;
          },
          OSM: function (layer, config) {
            var enc = $scope.encoders.layers["Layer"].call(this, layer);
            angular.extend(enc, {
              type: "OSM",
              baseURL: "http://a.tile.openstreetmap.org/",
              extension: "png",
              // Hack to return an extent for the base
              // layer in case of undefined
              maxExtent: layer.getExtent() || [
                -20037508.34, -20037508.34, 20037508.34, 20037508.34
              ],
              resolutions: layer.getSource().tileGrid.getResolutions(),
              tileSize: [
                layer.getSource().tileGrid.getTileSize(),
                layer.getSource().tileGrid.getTileSize()
              ]
            });
            return enc;
          },
          WMTS: function (layer, config) {
            var enc = $scope.encoders.layers["Layer"].call(this, layer);
            var source = layer.getSource();
            var tileGrid = source.getTileGrid();
            angular.extend(enc, {
              type: "WMTS",
              baseURL: location.protocol + "//wmts.geo.admin.ch", // FIXME
              layer: config.serverLayerName,
              maxExtent: layer.getExtent(),
              tileOrigin: tileGrid.getOrigin(),
              tileSize: [tileGrid.getTileSize(), tileGrid.getTileSize()],
              resolutions: tileGrid.getResolutions(),
              zoomOffset: tileGrid.getMinZoom(),
              version: "1.0.0",
              requestEncoding: "REST",
              formatSuffix: config.format || "jpeg",
              style: "default",
              dimensions: ["TIME"],
              params: { TIME: source.getDimensions().Time },
              matrixSet: "21781" // FIXME
            });

            return enc;
          }
        },
        legends: {
          ga_urllegend: function (layer, config) {
            var format = ".png";
            if ($scope.options.pdfLegendList.indexOf(layer.bodId) != -1) {
              format = pdfLegendString;
            }
            var enc = $scope.encoders.legends.base.call(this, config);
            enc.classes.push({
              name: "",
              icon:
                $scope.options.legendUrl + layer.bodId + "_" + $translate.use() + format
            });
            return enc;
          },
          base: function (config) {
            return {
              name: config.label,
              classes: []
            };
          }
        }
      };

      var getNearestScale = function (target, scales) {
        var nearest = null;
        angular.forEach(scales, function (scale) {
          if (nearest == null || Math.abs(scale - target) < Math.abs(nearest - target)) {
            nearest = scale;
          }
        });
        return nearest;
      };

      $scope.getConfig = function () {
        // http://mapfish.org/doc/print/protocol.html#print-pdf
        bodyEl.addClass(waitclass);
        var view = $scope.map.getView();
        var proj = view.getProjection();
        var lang = $translate.use();
        var defaultPage = {};
        defaultPage["lang" + lang] = true;
        var encLayers = [];
        var encLegends;
        var attributions = [];
        var layers = this.map.getLayers();
        pdfLegendsToDownload = [];

        angular.forEach(layers, function (layer) {
          layer.visible = true; // FIXME: was not set by default
          if (layer.visible) {
            var attribution = layer.attribution;
            if (attribution !== undefined && attributions.indexOf(attribution) == -1) {
              attributions.push(attribution);
            }
            if (layer instanceof ol.layer.Group) {
              var encs = $scope.encoders.layers["Group"].call(this, layer, proj);
              encLayers = encLayers.concat(encs);
            } else {
              var enc = encodeLayer(layer, proj);
              if (enc && enc.layer) {
                encLayers.push(enc.layer);
                if (enc.legend) {
                  encLegends = encLegends || [];
                  encLegends.push(enc.legend);
                }
              }
            }
          }
        });
        // FIXME this is a temporary solution
        var overlays = $scope.map.getOverlays();
        var resolution = $scope.map.getView().getResolution();

        overlays.forEach(function (overlay) {
          var center = overlay.getPosition();
          var offset = 5 * resolution;
          if (center) {
            var cross = {
              type: "Vector",
              styles: {
                1: {
                  externalGraphic: $scope.options.crossUrl,
                  graphicWidth: 16,
                  graphicHeight: 16
                }
              },
              styleProperty: "_gx_style",
              geoJson: {
                type: "FeatureCollection",
                features: [
                  {
                    type: "Feature",
                    properties: {
                      _gx_style: 1
                    },
                    geometry: {
                      type: "Point",
                      coordinates: [center[0], center[1], 0]
                    }
                  }
                ]
              },
              name: "drawing",
              opacity: 1
            };
            encLayers.push(cross);
          }
        });

        // scale = resolution * inches per map unit (m) * dpi
        var scale = parseInt(view.getResolution() * UNITS_RATIO * DPI2);
        var scales = this.scales.map(function (scale) {
          return parseInt(scale.value);
        });

        $scope.jsonSpec = {
          layout: this.layout.name,
          srs: proj.getCode(),
          units: proj.getUnits() || "m",
          rotation: -((view.getRotation() * 180.0) / Math.PI),
          lang: lang,
          dpi: this.dpi.value,
          outputFormat: "png",
          layers: encLayers,
          legends: encLegends,
          enableLegends: encLegends && encLegends.length > 0,
          pages: [
            angular.extend(
              {
                title: "", //default
                center: getPrintRectangleCenterCoord(),
                // scale has to be one of the advertise by the print server
                scale: getOptimalScale().value,
                dataOwner: "© " + attributions.join(),
                rotation: -((view.getRotation() * 180.0) / Math.PI)
              },
              defaultPage
            )
          ]
        };
        return $scope.jsonSpec;
      };

      var getPrintRectangleCenterCoord = function () {
        // Framebuffer size!!
        var bottomLeft = printRectangle.slice(0, 2);
        var width = printRectangle[2] - printRectangle[0];
        var height = printRectangle[3] - printRectangle[1];
        var center = [bottomLeft[0] + width / 2, bottomLeft[1] + height / 2];
        // convert back to map display size
        var mapPixelCenter = [
          center[0] / ol.has.DEVICE_PIXEL_RATIO,
          center[1] / ol.has.DEVICE_PIXEL_RATIO
        ];
        return $scope.map.getCoordinateFromPixel(mapPixelCenter);
      };

      var updatePrintRectanglePixels = function (scale) {
        if ($scope.mode === "thumbnailMaker") {
          printRectangle = calculatePageBoundsPixels(scale);
          $scope.map.render();
        }
      };

      var getOptimalScale = function () {
        var size = $scope.map.getSize();
        var resolution = $scope.map.getView().getResolution();
        var width = resolution * (size[0] - $scope.options.widthMargin * 2);
        var height = resolution * (size[1] - $scope.options.heightMargin * 2);
        var layoutSize = $scope.layout.map;
        var scaleWidth = (width * UNITS_RATIO * DPI) / layoutSize.width;
        var scaleHeight = (height * UNITS_RATIO * DPI) / layoutSize.height;
        var testScale = scaleWidth;
        if (scaleHeight < testScale) {
          testScale = scaleHeight;
        }
        var nextBiggest = null;
        //The algo below assumes that scales are sorted from
        //biggest (1:500) to smallest (1:2500000)
        angular.forEach($scope.scales, function (scale) {
          if (nextBiggest == null || testScale > scale.value) {
            nextBiggest = scale;
          }
        });
        return nextBiggest;
      };

      var calculatePageBoundsPixels = function (scale) {
        var s = parseFloat(scale.value);
        var size = $scope.layout.map; // papersize in dot!
        var view = $scope.map.getView();
        var center = view.getCenter();
        var resolution = view.getResolution();
        var w = ((((size.width / DPI) * MM_PER_INCHES) / 1000.0) * s) / resolution;
        var h = ((((size.height / DPI) * MM_PER_INCHES) / 1000.0) * s) / resolution;
        var mapSize = $scope.map.getSize();
        var center = [
          (mapSize[0] * ol.has.DEVICE_PIXEL_RATIO) / 2,
          (mapSize[1] * ol.has.DEVICE_PIXEL_RATIO) / 2
        ];

        var minx, miny, maxx, maxy;

        minx = center[0] - w / 2;
        miny = center[1] - h / 2;
        maxx = center[0] + w / 2;
        maxy = center[1] + h / 2;
        return [minx, miny, maxx, maxy];
      };

      $scope.$watch("mode", function (newVal, oldVal) {
        if (newVal === "thumbnailMaker") {
          activate();
        } else {
          deactivate();
        }
      });
    }
  ]);

  module.directive("gaPrint", [
    "gnCurrentEdit",
    function (gnCurrentEdit) {
      return {
        restrict: "A",
        templateUrl: "../../catalog/components/common/map/" + "print/partials/print.html",
        controller: "GaPrintDirectiveController",
        link: function (scope, elt, attrs, controller) {
          scope.defaultLayout = attrs.layout;
          scope.gnCurrentEdit = gnCurrentEdit;
        }
      };
    }
  ]);
})();
