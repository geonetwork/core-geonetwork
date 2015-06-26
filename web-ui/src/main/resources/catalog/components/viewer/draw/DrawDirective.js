(function() {
  goog.provide('gn_draw');

  var module = angular.module('gn_draw', [
  ]);

  function readAsText(f, callback) {
    try {
      var reader = new FileReader();
      reader.readAsText(f);
      reader.onload = function(e) {
        if (e.target && e.target.result) {
          callback(e.target.result);
        } else {
          console.error('File could not be loaded');
        }
      };
      reader.onerror = function(e) {
        console.error('File could not be read');
      };
    } catch (e) {
      console.error('File could not be read');
    }
  }

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnDraw
   *
   * @description
   * Panel that provides tools to draw and create annotations:
   * <ul>
   *   <li>point</li>
   *   <li>linestring</li>
   *   <li>polygon</li>
   *   <li>text</li>
   */
  module.directive('gnDraw', [
    'ngeoDecorateInteraction',
    function(ngeoDecorateInteraction) {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/viewer/draw/' +
            'partials/draw.html',
        scope: {
          map: '=',
          vector: '='
        },
        link: function(scope, element, attrs) {
          var map = scope.map;
          var source = new ol.source.Vector();

          var txtStyleCache = {};
          var featureStyle = new ol.style.Style({
            fill: new ol.style.Fill({
              color: 'rgba(255, 255, 255, 0.4)'
            }),
            stroke: new ol.style.Stroke({
              color: '#ffcc33',
              width: 2
            }),
            image: new ol.style.Circle({
              radius: 7,
              fill: new ol.style.Fill({
                color: '#ffcc33'
              })
            })
          });

          var textStyleCfg = {
            fill: {
              color: 'rgba(255, 255, 255, 0.6)'
            },
            stroke: {
              color: '#319FD3',
              width: 1,
              lineDash: [0]
            },
            text: {
              font: '14px Calibri,sans-serif',
              fill: {
                color: '#000'
              },
              stroke: {
                color: '#fff',
                width: 3
              }
            }
          };
          scope.featureStyleCfg = {
            fill: {
              color: 'rgba(255, 255, 255, 0.6)'
            },
            stroke: {
              color: '#ff0000',
              width: 1
            },
            image: {
              radius: 7,
              fill: {
                color: '#ffcc33'
              }
            },
            text: {
              font: '14px Calibri,sans-serif',
              fill: {
                color: '#000'
              },
              stroke: {
                color: '#fff',
                width: 3
              }
            }

          };


          var textFeatStyleFn = function(feature) {
            var f;
            if (feature instanceof ol.Feature) {
              f = feature;
            }
            else {
              f = this;
            }
            var text = f.get('name');
            if (!txtStyleCache[text]) {
              txtStyleCache[text] = [new ol.style.Style({
                fill: new ol.style.Fill({
                  color: textStyleCfg.fill.color
                }),
                stroke: new ol.style.Stroke({
                  color: textStyleCfg.stroke.color,
                  width: textStyleCfg.stroke.width
                }),
                text: new ol.style.Text({
                  font: scope.featureStyleCfg.text.font,
                  text: text,
                  fill: new ol.style.Fill({
                    color: scope.featureStyleCfg.text.fill.color
                  }),
                  stroke: new ol.style.Stroke({
                    color: scope.featureStyleCfg.text.stroke.color,
                    width: scope.featureStyleCfg.text.stroke.width
                  })
                })
              })];
            }
            return txtStyleCache[text];
          };

          var drawTextStyleFn = function(feature, resolution) {
            return [new ol.style.Style({
              fill: new ol.style.Fill({
                color: textStyleCfg.fill.color
              }),
              stroke: new ol.style.Stroke({
                color: textStyleCfg.stroke.color,
                width: textStyleCfg.stroke.width
              }),
              text: new ol.style.Text({
                font: textStyleCfg.text.font,
                text: scope.text,
                fill: new ol.style.Fill({
                  color: textStyleCfg.text.fill.color
                }),
                stroke: new ol.style.Stroke({
                  color: textStyleCfg.text.stroke.color,
                  width: textStyleCfg.text.stroke.width
                })
              })
            })];
          };

          var vector = new ol.layer.Vector({
            source: source,
            style: featureStyle,
            temporary: true
          });
          scope.vector = vector;

          var onDrawend = function(evt) {
            if (evt) {
              var style = scope.featureStyleCfg;
              evt.feature.setStyle(new ol.style.Style({
                fill: new ol.style.Fill({
                  color: style.fill.color
                }),
                stroke: new ol.style.Stroke({
                  color: style.stroke.color,
                  width: style.stroke.width
                }),
                image: new ol.style.Circle({
                  radius: style.image.radius,
                  fill: new ol.style.Fill({
                    color: style.image.fill.color
                  })
                })
              }));
            }
            scope.$apply();
          };

          var drawPolygon = new ol.interaction.Draw(({
            type: 'Polygon',
            source: source
          }));
          drawPolygon.on('drawend', onDrawend);
          ngeoDecorateInteraction(drawPolygon, map);
          drawPolygon.active = false;
          scope.drawPolygon = drawPolygon;
          map.addInteraction(drawPolygon);

          var drawPoint = new ol.interaction.Draw(({
            type: 'Point',
            source: source
          }));
          drawPoint.on('drawend', onDrawend);
          ngeoDecorateInteraction(drawPoint, map);
          drawPoint.active = false;
          scope.drawPoint = drawPoint;
          map.addInteraction(drawPoint);

          var drawLine = new ol.interaction.Draw(({
            type: 'LineString',
            source: source
          }));
          drawLine.on('drawend', onDrawend);
          ngeoDecorateInteraction(drawLine, map);
          drawLine.active = false;
          scope.drawLine = drawLine;
          map.addInteraction(drawLine);

          var drawText = new ol.interaction.Draw(({
            type: 'Point',
            source: source,
            style: drawTextStyleFn
          }));
          drawText.on('drawend', function(evt) {
            evt.feature.set('name', scope.text);
            evt.feature.setStyle(textFeatStyleFn);
            onDrawend();
          });
          ngeoDecorateInteraction(drawText, map);
          drawText.active = false;
          scope.drawText = drawText;
          map.addInteraction(drawText);

          var select = new ol.interaction.Select();
          var modify = new ol.interaction.Modify({
            features: select.getFeatures()
          });

          /*
          function saves the currently drawn geometries as geoJSON
          inspired by http://openlayers.org/en/v3.4.0/examples/kml.html
          todo: allow a user to select an export format (kml/gml/json)
          */
          scope.save = function($event) {
            var exportElement = document.getElementById('export-geom');
            if ('download' in exportElement) {
              var vectorSource = scope.vector.getSource();
              var features = [];

              vectorSource.forEachFeature(function(feature) {
                var clone = feature.clone();
                clone.setId(feature.getId());
                // geoJson commonly uses wgs84
                // (view usually has spherical mercator)
                clone.getGeometry().transform(
                    map.getView().getProjection(), 'EPSG:4326');

                // Save the feature style
                var st = feature.getStyle();
                if (angular.isFunction(st)) {
                  st = st(feature)[0];
                }

                var styleObj = {
                  fill: {
                    color: st.getFill().getColor()
                  },
                  stroke: {
                    color: st.getStroke().getColor(),
                    width: st.getStroke().getWidth()
                  }
                };
                if (st.getText()) {
                  styleObj.text = {
                    text: st.getText().getText(),
                    font: st.getText().getFont(),
                    stroke: {
                      color: st.getText().getStroke().getColor(),
                      width: st.getText().getStroke().getWidth()
                    },
                    fill: {
                      color: st.getText().getFill().getColor()
                    }
                  };
                }
                clone.set('_style', styleObj);
                features.push(clone);
              });

              var string = new ol.format.GeoJSON().writeFeatures(features);
              //requires /lib/base64.js
              var base64 = base64EncArr(strToUTF8Arr(string));
              exportElement.href =
                  'data:application/vnd.geo+json;base64,' + base64;
            }
          };

          var fileInput = element.find('input[type="file"]')[0];
          element.find('.import').click(function() {
            fileInput.click();
          });

          angular.element(fileInput).bind('change', function(changeEvent) {
            if (fileInput.files.length > 0) {
              readAsText(fileInput.files[0], function(text) {
                var features = new ol.format.GeoJSON().readFeatures(text, {
                  dataProjection: 'EPSG:4326',
                  featureProjection: map.getView().getProjection()
                });

                // Set each feature its style
                angular.forEach(features, function(f, i) {
                  var st = f.get('_style');
                  var style = new ol.style.Style({
                    fill: new ol.style.Fill({
                      color: st.fill.color
                    }),
                    stroke: new ol.style.Stroke({
                      color: st.stroke.color,
                      width: st.stroke.width
                    }),
                    text: st.text ? new ol.style.Text({
                      font: st.text.font,
                      text: st.text.text,
                      fill: new ol.style.Fill({
                        color: st.text.fill.color
                      }),
                      stroke: new ol.style.Stroke({
                        color: st.text.stroke.color,
                        width: st.text.stroke.width
                      })
                    }) : undefined
                  });
                  f.setStyle(style);
                });
                source.addFeatures(features);
                $('#features-file-input')[0].value = '';
                scope.$digest();
              });
            }
          });


          var deleteF = new ol.interaction.Select();
          deleteF.getFeatures().on('add',
              function(evt) {
                vector.getSource().removeFeature(evt.element);
                this.clear();
                scope.$apply();
              });
          ngeoDecorateInteraction(deleteF, map);
          deleteF.active = false;
          scope.deleteF = deleteF;
          map.addInteraction(deleteF);

          Object.defineProperty(scope, 'modifying', {
            get: function() {
              return (map.getInteractions().getArray().indexOf(select) >= 0 &&
                  map.getInteractions().getArray().indexOf(modify) >= 0);
            },
            set: function(val) {
              if (val) {
                map.addInteraction(select);
                map.addInteraction(modify);
              } else {
                map.removeInteraction(select);
                map.removeInteraction(modify);
                select.getFeatures().clear();
              }
            }
          });

          Object.defineProperty(vector, 'inmap', {
            get: function() {
              return map.getLayers().getArray().indexOf(vector) >= 0;
            },
            set: function(val) {
              if (val) {
                map.addLayer(vector);
              } else {
                drawPolygon.active = false;
                drawPoint.active = false;
                drawLine.active = false;
                drawText.active = false;
                deleteF = false;
                scope.modifying = false;
              }
            }
          });

          scope.getActiveDrawType = function() {
            if (scope.drawPoint.active) return 'point';
            else if (scope.drawLine.active) return 'line';
            else if (scope.drawPolygon.active) return 'polygon';
            else if (scope.drawText.active) return 'text';
          };

          scope.$on('owsContextReseted', function() {
            source.clear();
          });
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnStyleForm
   *
   * @description
   * Form to edit features style. The form content depends on gnStyleType
   * attribute
   */
  module.directive('gnStyleForm', [
    function() {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/viewer/draw/' +
            'partials/styleform.html',
        scope: {
          style: '=gnStyleForm',
          getType: '&gnStyleType'
        },
        link: function(scope, element, attrs) {
          scope.colors = {
            red: '#FF0000',
            orange: '#FFA500',
            blue: '#0000FF',
            white: '#FFFFFF',
            black: '#000000',
            gray: '#BEBEBE',
            yellow: '#FFFF00',
            green: '#008000',
            pink: '#FFC0CB',
            purple: '#800080',
            brown: '#A52A2A'
          };
        }
      };
    }]);

})();
