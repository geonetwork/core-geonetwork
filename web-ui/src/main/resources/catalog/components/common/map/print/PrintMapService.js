(function() {
  goog.provide('gn_printmap_service');

  var module = angular.module('gn_printmap_service', []);

  module.service('gnPrint', function() {

    var self = this;

    var options = {
      heightMargin: 0,
      widthMargin: 0
    };

    var DPI = 72;
    var MM_PER_INCHES = 25.4;
    var UNITS_RATIO = 39.37;

    /**
     * Get the map coordinates of the center of the given print rectangle.
     * @param {ol.map} map
     * @param {Array} printRectangle
     * @return {*}
     */
    this.getPrintRectangleCenterCoord = function(map, printRectangle) {
      // Framebuffer size!!
      var bottomLeft = printRectangle.slice(0, 2);
      var width = printRectangle[2] - printRectangle[0];
      var height = printRectangle[3] - printRectangle[1];
      var center = [bottomLeft[0] + width / 2, bottomLeft[1] + height / 2];
      // convert back to map display size
      var mapPixelCenter = [center[0] / ol.has.DEVICE_PIXEL_RATIO,
        center[1] / ol.has.DEVICE_PIXEL_RATIO];
      return map.getCoordinateFromPixel(mapPixelCenter);
    };

    /**
     * Compute the bounds (in map coordinates) of the print area depending
     * on the print scale and layout.
     * @param {ol.map} map
     * @param {Object} layout
     * @param {Object} scale
     * @return {*[]} extent
     */
    this.calculatePageBoundsPixels = function(map, layout, scale) {
      var s = parseFloat(scale.value);
      var size = layout.map; // papersize in dot!
      var view = map.getView();
      var center = view.getCenter();
      var resolution = view.getResolution();
      var w = size.width / DPI * MM_PER_INCHES / 1000.0 * s / resolution;
      var h = size.height / DPI * MM_PER_INCHES / 1000.0 * s / resolution;
      var mapSize = map.getSize();
      var center = [mapSize[0] * ol.has.DEVICE_PIXEL_RATIO / 2 ,
        mapSize[1] * ol.has.DEVICE_PIXEL_RATIO / 2];

      var minx, miny, maxx, maxy;

      minx = center[0] - (w / 2);
      miny = center[1] - (h / 2);
      maxx = center[0] + (w / 2);
      maxy = center[1] + (h / 2);
      return [minx, miny, maxx, maxy];
    };

    /**
     * Get the optimal print scale (from an array of scales) depending on
     * the print layout and dpi
     * @param {ol.map} map
     * @param {Object} scales
     * @param {Object} layout
     * @return {*}
     */
    this.getOptimalScale = function(map, scales, layout) {
      var size = map.getSize();
      var resolution = map.getView().getResolution();
      var width = resolution * (size[0] - (options.widthMargin * 2));
      var height = resolution * (size[1] - (options.heightMargin * 2));
      var layoutSize = layout.map;
      var scaleWidth = width * UNITS_RATIO * DPI / layoutSize.width;
      var scaleHeight = height * UNITS_RATIO * DPI / layoutSize.height;
      var testScale = scaleWidth;
      if (scaleHeight < testScale) {
        testScale = scaleHeight;
      }
      var nextBiggest = null;
      //The algo below assumes that scales are sorted from
      //biggest (1:500) to smallest (1:2500000)
      angular.forEach(scales, function(scale) {
        if (nextBiggest == null ||
            testScale > scale.value) {
          nextBiggest = scale;
        }
      });
      return nextBiggest;
    };

    /**
     * Object of methods that encode ol3 layers into print config objects.
     *
     * @type {{layers: {Layer: 'Layer', Group: 'Group',
     *    Vector: 'Vector', WMS: 'WMS',
     *    OSM: 'OSM', WMTS: 'WMTS'}, legends: {ga_urllegend: 'ga_urllegend',
     *    base: 'base'}}}
     */
    this.encoders = {
      'layers': {
        'Layer': function(layer) {
          var enc = {
            layer: layer.bodId,
            opacity: layer.getOpacity()
          };
          return enc;
        },
        'Group': function(layer, proj) {
          var encs = [];
          var subLayers = layer.getLayers();
          subLayers.forEach(function(subLayer, idx, arr) {
            if (subLayer.visible) {
              var enc = self.encoders.
                  layers['Layer'].call(this, layer);
              var layerEnc = encodeLayer(subLayer, proj);
              if (layerEnc && layerEnc.layer) {
                $.extend(enc, layerEnc);
                encs.push(enc.layer);
              }
            }
          });
          return encs;
        },
        'Vector': function(layer, features) {
          var enc = self.encoders.
              layers['Layer'].call(this, layer);
          var format = new ol.format.GeoJSON();
          var encStyles = {};
          var encFeatures = [];
          var stylesDict = {};
          var styleId = 0;
          var hasLayerStyleFunction = !!(layer.getStyleFunction &&
              layer.getStyleFunction());

          angular.forEach(features, function(feature) {
            var encStyle = {
              id: styleId
            };
            var styles = (hasLayerStyleFunction) ?
                layer.getStyleFunction()(feature) :
                ol.feature.defaultStyleFunction(feature);


            var geometry = feature.getGeometry();

            // Transform an ol.geom.Circle to a ol.geom.Polygon
            if (geometry.getType() === 'Circle') {
              var polygon = circleToPolygon(geometry);
              feature = new ol.Feature(polygon);
            }

            var encJSON = format.writeFeature(feature);
            if (!encJSON.properties) {
              encJSON.properties = {};

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
            type: 'Vector',
            styles: encStyles,
            styleProperty: '_gx_style',
            geoJson: {
              type: 'FeatureCollection',
              features: encFeatures
            },
            name: layer.bodId,
            opacity: (layer.opacity != null) ? layer.opacity : 1.0
          });
          return enc;
        },
        'WMS': function(layer, config) {
          var enc = self.encoders.
              layers['Layer'].call(this, layer);
          var params = layer.getSource().getParams();
          var layers = params.LAYERS.split(',') || [];
          var styles = (params.STYLES !== undefined) ?
              params.STYLES.split(',') :
              new Array(layers.length).join(',').split(',');
          var url = layer instanceof ol.source.ImageWMS ?
              layer.getSource().getUrl() :
              layer.getSource().getUrls()[0];
          angular.extend(enc, {
            type: 'WMS',
            baseURL: config.wmsUrl || url,
            layers: layers,
            styles: styles,
            format: 'image/' + (config.format || 'png'),
            customParams: {
              'EXCEPTIONS': 'XML',
              'TRANSPARENT': 'true',
              'CRS': 'EPSG:3857',
              'TIME': params.TIME
            },
            singleTile: config.singleTile || false
          });
          return enc;
        },
        'OSM': function(layer, config) {
          var enc = self.encoders.
              layers['Layer'].call(this, layer);
          angular.extend(enc, {
            type: 'OSM',
            baseURL: 'http://a.tile.openstreetmap.org/',
            extension: 'png',
            // Hack to return an extent for the base
            // layer in case of undefined
            maxExtent: layer.getExtent() ||
                [-20037508.34, -20037508.34, 20037508.34, 20037508.34],
            resolutions: layer.getSource().tileGrid.getResolutions(),
            tileSize: [
              layer.getSource().tileGrid.getTileSize(),
              layer.getSource().tileGrid.getTileSize()]
          });
          return enc;
        },
        'Bing': function(layer, config) {
          var enc = self.encoders.
              layers['Layer'].call(this, layer);
          angular.extend(enc, {
            type: 'OSM',
            baseURL: 'http://a.tile.openstreetmap.org/',
            extension: 'png',
            // Hack to return an extent for the base
            // layer in case of undefined
            maxExtent: layer.getExtent() ||
                [-20037508.34, -20037508.34, 20037508.34, 20037508.34],
            resolutions: layer.getSource().tileGrid.getResolutions(),
            tileSize: [
              layer.getSource().tileGrid.getTileSize(),
              layer.getSource().tileGrid.getTileSize()]
          });
          return enc;
        },
        'MapQuest': function(layer, config) {
          var enc = self.encoders.
              layers['Layer'].call(this, layer);
          angular.extend(enc, {
            type: 'OSM',
            baseURL: 'http://otile1-s.mqcdn.com/tiles/1.0.0/osm',
            extension: 'png',
            // Hack to return an extent for the base
            // layer in case of undefined
            maxExtent: layer.getExtent() ||
                [-20037508.34, -20037508.34, 20037508.34, 20037508.34],
            resolutions: layer.getSource().tileGrid.getResolutions(),
            tileSize: [
              layer.getSource().tileGrid.getTileSize(),
              layer.getSource().tileGrid.getTileSize()]
          });
          return enc;
        },
        'WMTS': function(layer, config) {
          // sextant specific
          var enc = self.encoders.layers['Layer'].
              call(this, layer);
          var source = layer.getSource();
          var tileGrid = source.getTileGrid();
          var matrixSet = 'EPSG:3857';
          var matrixIds = new Array(tileGrid.getResolutions().length);
          for (var z = 0; z < tileGrid.getResolutions().length; ++z) {
            matrixIds[z] = {
              identifier: tileGrid.getMatrixIds()[z],
              resolution: tileGrid.getResolutions()[z],
              tileSize: [tileGrid.getTileSize(), tileGrid.getTileSize()],
              topLeftCorner: tileGrid.getOrigin(),
              matrixSize: [Math.pow(2, z), Math.pow(2, z)]
            };
          }

          angular.extend(enc, {
            type: 'WMTS',
            baseURL: 'http://visi-sextant.ifremer.fr:8080/' +
                'geowebcache/service/wmts?',
            layer: 'Sextant',
            version: '1.0.0',
            requestEncoding: 'KVP',
            format: 'image/png',
            style: 'default',
            matrixSet: matrixSet,
            matrixIds: matrixIds
          });

          return enc;
        }
      },
      'legends' : {
        'ga_urllegend': function(layer, config) {
          var format = '.png';
          if ($scope.options.pdfLegendList.indexOf(layer.bodId) != -1) {
            format = pdfLegendString;
          }
          var enc = self.encoders.legends.base.call(this, config);
          enc.classes.push({
            name: '',
            icon: $scope.options.legendUrl +
                layer.bodId + '_' + $translate.uses() + format
          });
          return enc;
        },
        'base': function(config) {
          return {
            name: config.label,
            classes: []
          };
        }
      }
    };

    // Transform an ol.Color to an hexadecimal string
    var toHexa = function(olColor) {
      var hex = '#';
      for (var i = 0; i < 3; i++) {
        var part = olColor[i].toString(16);
        if (part.length === 1 && parseInt(part) < 10) {
          hex += '0';
        }
        hex += part;
      }
      return hex;
    };

    // Transform a ol.style.Style to a print literal object
    var transformToPrintLiteral = function(feature, style) {
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

      if (imageStyle && type == 'Point') {
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
          literal.fillOpacity = 1;
        } else { // ol.style.Circle
          fill = imageStyle.getFill();
          stroke = imageStyle.getStroke();
          literal.pointRadius = imageStyle.getRadius();
        }
      }

      if (fill) {
        var color = ol.color.asArray(fill.getColor());
        literal.fillColor = toHexa(color);
        literal.fillOpacity = color[3];
      } else if (!literal.fillOpacity) {
        literal.fillOpacity = 0; // No fill
      }

      if (stroke) {
        var color = ol.color.asArray(stroke.getColor());
        literal.strokeWidth = stroke.getWidth();
        literal.strokeColor = toHexa(color);
        literal.strokeOpacity = color[3];
        literal.strokeLinecap = stroke.getLineCap() || 'round';
        literal.strokeLinejoin = stroke.getLineJoin() || 'round';

        if (stroke.getLineDash()) {
          literal.strokeDashstyle = 'dash';
        }
        // TO FIX: Not managed by the print server
        // literal.strokeMiterlimit = stroke.getMiterLimit();
      } else {
        literal.strokeOpacity = 0; // No Stroke
      }

      if (textStyle) {
        var fillColor = ol.color.asArray(textStyle.getFill().getColor());
        var strokeColor = ol.color.asArray(textStyle.getStroke().getColor());
        var fontValues = textStyle.getFont().split(' ');
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

  });
})();
