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
  goog.provide("gn_draw");

  var module = angular.module("gn_draw", ["color.picker"]);

  function readAsText(f, callback) {
    try {
      var reader = new FileReader();
      reader.readAsText(f);
      reader.onload = function (e) {
        if (e.target && e.target.result) {
          callback(e.target.result);
        } else {
          console.error("File could not be loaded");
        }
      };
      reader.onerror = function (e) {
        console.error("File could not be read");
      };
    } catch (e) {
      console.error("File could not be read");
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
  module.directive("gnDraw", [
    "olDecorateInteraction",
    function (olDecorateInteraction) {
      return {
        restrict: "A",
        replace: false,
        templateUrl: "../../catalog/components/viewer/draw/" + "partials/draw.html",
        scope: {
          map: "=",
          vector: "=",
          saveCallback: "&?"
        },
        link: function (scope, element, attrs) {
          var map = scope.map;
          var source = new ol.source.Vector();

          /**
           * Style function of the drawn features vector.
           * The feature styles are stored in a `_style` attribute instead of
           * directly in its style to avoid to sotre style in the feature (cause
           * it will have priority on select layer style function).
           *
           * @param {ol.Feature} feature
           * @return {*[]} styles array
           */
          var drawVectorStyleFn = function (feature) {
            if (feature.get("_style")) {
              return [feature.get("_style")];
            }
          };

          /**
           * Style function of the select interaction intern layer.
           * The slected feature will take same style as original feature but
           * will also display the vertexes (expect for text).
           *
           * @param {ol.Feature} feature
           * @return {*[]} styles array
           */
          var selectVectorStyleFn = function (feature) {
            var drawType = feature.get("_type");
            if (feature.get("_style")) {
              var fStyle = feature.get("_style");

              // If text, just display text
              if (drawType === "text") {
                return [fStyle];
              }
              var selectStyle = new ol.style.Style({
                image: new ol.style.Circle(
                  feature.getGeometry().getType() == "Point"
                    ? {
                        radius: fStyle.getImage().getRadius() + 5,
                        fill: new ol.style.Fill({
                          color: fStyle.getImage().getFill().getColor()
                        })
                      }
                    : {
                        radius: fStyle.getStroke().getWidth() + 5,
                        fill: new ol.style.Fill({
                          color: fStyle.getStroke().getColor()
                        })
                      }
                ),
                // Draw the vertexes of the features
                geometry: function (feature) {
                  var coordinates;
                  var geom = feature.getGeometry();
                  if (geom.getType() == "Polygon") {
                    coordinates = geom.getCoordinates()[0];
                  } else if (geom.getType() == "LineString") {
                    coordinates = geom.getCoordinates();
                  } else {
                    return geom;
                  }
                  return new ol.geom.MultiPoint(coordinates);
                }
              });
              return [fStyle, selectStyle];
            }
          };

          // Mapping with style form
          scope.featureStyleCfg = {
            fill: {
              color: "rgba(255, 255, 255, 0.6)"
            },
            stroke: { color: "#ff0000", width: 1 },
            image: { radius: 7, fill: { color: "#ffcc33" } },
            text: {
              width: 14,
              fill: { color: "#000" },
              stroke: { color: "#fff", width: 2 }
            }
          };

          /**
           * Style fn just use for the text overlay before it's drawn.
           * @return {*[]} the style corresponding to text style form
           */
          var drawTextStyleFn = function () {
            var style = scope.featureStyleCfg;
            return [
              new ol.style.Style({
                fill: new ol.style.Fill({
                  color: style.fill.color
                }),
                stroke: new ol.style.Stroke({
                  color: style.stroke.color,
                  width: style.stroke.width
                }),
                text: new ol.style.Text({
                  font: style.text.font,
                  text: style.text.text,
                  fill: new ol.style.Fill({
                    color: style.text.fill.color
                  }),
                  stroke: new ol.style.Stroke({
                    color: style.text.stroke.color,
                    width: style.text.stroke.width
                  })
                })
              })
            ];
          };

          // The vector that will contains all drawn features
          var vector = new ol.layer.Vector({
            source: source,
            temporary: true,
            style: drawVectorStyleFn
          });
          scope.vector = vector;

          /**
           * Create a `ol.style.Style` object from style config mapped with
           * style form.
           *
           * @param {ol.Feature} feature to know if it's text or not
           * @param {object} styleCfg the style config object
           * @return {ol.style.Style} the ol style
           */
          var createStyleFromConfig = function (feature, styleCfg) {
            var drawType = feature.get("_type");
            var styleObjCfg = {
              fill: new ol.style.Fill({
                color: styleCfg.fill.color
              }),
              stroke: new ol.style.Stroke({
                color: styleCfg.stroke.color,
                width: styleCfg.stroke.width
              })
            };

            // It is a Text feature
            if (drawType === "text") {
              styleObjCfg.text = new ol.style.Text({
                font: styleCfg.text.font,
                text: styleCfg.text.text,
                fill: new ol.style.Fill({
                  color: styleCfg.text.fill.color
                }),
                stroke:
                  styleCfg.text.stroke.width > 0
                    ? new ol.style.Stroke({
                        color: styleCfg.text.stroke.color,
                        width: styleCfg.text.stroke.width
                      })
                    : undefined
              });
            } else if (drawType === "point") {
              styleObjCfg.image = new ol.style.Circle({
                radius: styleCfg.image.radius,
                fill: new ol.style.Fill({
                  color: styleCfg.image.fill.color
                })
              });
            }
            return new ol.style.Style(styleObjCfg);
          };

          /**
           * Serialize an `ol.style.Style` to a JSON object.
           * Used to store the style as feature property in the GeoJSON.
           * @param {ol.Feature} feature
           * @return {Object} serialized style
           */
          var getStyleObjFromFeature = function (feature) {
            var st = feature.get("_style");
            var drawType = feature.get("_type");
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
                },
                width: parseInt(
                  new RegExp("([0-9]{1,3})(?=px)").exec(st.getText().getFont())[0]
                )
              };
            } else if (drawType === "point") {
              styleObj.image = {
                radius: st.getImage().getRadius(),
                fill: {
                  color: st.getImage().getFill().getColor()
                }
              };
            }
            return styleObj;
          };

          /**
           * Called for each draw end (point, line, polygon, text).
           * Create the `ol.style.Style` depending on style form and attach
           * it to the feature as a `_style` parameter.
           * This is done not to save style into the feature so the modify
           * layer style Fn is not overloaded.
           * Also stores the current draw type on the feature
           *
           * @param {object} evt ol3 event draw end
           * @param {string} drawType either 'point', 'text', 'line' or 'polygon'
           */
          var onDrawend = function (evt, drawType) {
            var f = evt.feature;
            f.set("_type", drawType);
            f.set("_style", createStyleFromConfig(f, scope.featureStyleCfg));
            scope.$apply();
          };

          // Draw interactions
          var drawPolygon = new ol.interaction.Draw({
            type: "Polygon",
            source: source
          });
          drawPolygon.on("drawend", function (evt) {
            onDrawend(evt, "polygon");
          });
          olDecorateInteraction(drawPolygon, map);
          drawPolygon.active = false;
          scope.drawPolygon = drawPolygon;
          map.addInteraction(drawPolygon);

          var drawPoint = new ol.interaction.Draw({
            type: "Point",
            source: source
          });
          drawPoint.on("drawend", function (evt) {
            onDrawend(evt, "point");
          });
          olDecorateInteraction(drawPoint, map);
          drawPoint.active = false;
          scope.drawPoint = drawPoint;
          map.addInteraction(drawPoint);

          var drawLine = new ol.interaction.Draw({
            type: "LineString",
            source: source
          });
          drawLine.on("drawend", function (evt) {
            onDrawend(evt, "line");
          });
          olDecorateInteraction(drawLine, map);
          drawLine.active = false;
          scope.drawLine = drawLine;
          map.addInteraction(drawLine);

          var drawText = new ol.interaction.Draw({
            type: "Point",
            source: source,
            style: drawTextStyleFn
          });
          drawText.on("drawend", function (evt) {
            onDrawend(evt, "text");
          });
          olDecorateInteraction(drawText, map);
          drawText.active = false;
          scope.drawText = drawText;
          map.addInteraction(drawText);

          scope.interactions = [
            {
              interaction: drawPoint,
              label: "Point"
            },
            {
              interaction: drawLine,
              label: "Linestring"
            },
            {
              interaction: drawPolygon,
              label: "Polygon"
            },
            {
              interaction: drawText,
              label: "Text"
            }
          ];

          // Manage selection
          var select = new ol.interaction.Select({
            multi: false,
            style: selectVectorStyleFn
          });
          var modify = new ol.interaction.Modify({
            features: select.getFeatures()
          });

          var unregisterSelectFn;
          select.getFeatures().on("change:length", function (evt) {
            scope.editedFeature = select.getFeatures().item(0);
            if (scope.editedFeature) {
              angular.extend(
                scope.featureStyleCfg,
                getStyleObjFromFeature(scope.editedFeature)
              );
              unregisterSelectFn = scope.$watch(
                "featureStyleCfg",
                function (sCfg) {
                  scope.editedFeature.set(
                    "_style",
                    createStyleFromConfig(scope.editedFeature, sCfg)
                  );
                },
                true
              );
            } else {
              unregisterSelectFn && unregisterSelectFn();
            }
            scope.$applyAsync();
          });

          scope.getActiveDrawLabel = function () {
            for (var i = 0; i < scope.interactions.length; i++) {
              if (scope.interactions[i].interaction.active) {
                return scope.interactions[i].label;
              }
            }
            return "add";
          };

          /*
          function saves the currently drawn geometries as geoJSON
          inspired by http://openlayers.org/en/v3.4.0/examples/kml.html
          todo: allow a user to select an export format (kml/gml/json)
          */
          scope.save = function ($event) {
            var vectorSource = scope.vector.getSource();
            var features = [];

            vectorSource.forEachFeature(function (feature) {
              var clone = feature.clone();
              clone.setId(feature.getId());
              // geoJson commonly uses wgs84
              // (view usually has spherical mercator)
              clone.getGeometry().transform(map.getView().getProjection(), "EPSG:4326");

              // Save the feature style
              clone.set("_style", getStyleObjFromFeature(feature));
              clone.set("_type", feature.get("_type"));
              features.push(clone);
            });

            var string = new ol.format.GeoJSON().writeFeatures(features);

            if (scope.saveCallback) {
              scope.saveCallback({
                json: string
              });
            } else {
              var exportElement = document.getElementById("export-geom");
              if ("download" in exportElement) {
                //requires /lib/base64.js
                var base64 = base64EncArr(strToUTF8Arr(string));
                exportElement.href = "data:application/vnd.geo+json;base64," + base64;
              }
            }
          };

          var fileInput = element.find('input[type="file"]')[0];
          element.find(".import").click(function () {
            fileInput.click();
          });

          /**
           * Load features file
           */
          angular.element(fileInput).bind("change", function (changeEvent) {
            if (fileInput.files.length > 0) {
              readAsText(fileInput.files[0], function (text) {
                var features = new ol.format.GeoJSON().readFeatures(text, {
                  dataProjection: "EPSG:4326",
                  featureProjection: map.getView().getProjection()
                });

                // Set each feature its style
                angular.forEach(features, function (f, i) {
                  // for backwards compatiblity (features used to have text stores in the 'name' prop,
                  // and not their type)
                  if (!f.get("_type")) {
                    if (f.get("name")) {
                      f.set("_type", "text");
                      var style = f.get("_style");
                      style.text.text = f.get("name");
                      f.set("name", undefined);
                    } else if (f.getGeometry().getType() === "Point")
                      f.set("_type", "point");
                    else if (f.getGeometry().getType() === "Polygon")
                      f.set("_type", "polygon");
                    else if (f.getGeometry().getType() === "LineString")
                      f.set("_type", "line");
                  }
                  f.set("_style", createStyleFromConfig(f, f.get("_style")));
                });
                source.addFeatures(features);
                $("#features-file-input")[0].value = "";
                scope.$digest();
              });
            }
          });

          var deleteF = new ol.interaction.Select();
          deleteF.getFeatures().on("add", function (evt) {
            vector.getSource().removeFeature(evt.element);
            this.clear();
            scope.$apply();
          });
          olDecorateInteraction(deleteF, map);
          deleteF.active = false;
          scope.deleteF = deleteF;
          map.addInteraction(deleteF);

          Object.defineProperty(scope, "modifying", {
            get: function () {
              return (
                map.getInteractions().getArray().indexOf(select) >= 0 &&
                map.getInteractions().getArray().indexOf(modify) >= 0
              );
            },
            set: function (val) {
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

          Object.defineProperty(vector, "active", {
            get: function () {
              return map.getLayers().getArray().indexOf(vector) >= 0 && scope.active;
            },
            set: function (val) {
              if (val) {
                // this is simply used to indicate that the draw
                // directive is active (ie panel is opened in the UI)
                scope.active = true;

                // only add layer if it is not already here
                if (map.getLayers().getArray().indexOf(vector) < 0) {
                  map.addLayer(vector);
                }
              } else {
                drawPolygon.active = false;
                drawPoint.active = false;
                drawLine.active = false;
                drawText.active = false;
                deleteF = false;
                scope.modifying = false;
                scope.active = false;
              }
            }
          });

          scope.getActiveDrawType = function () {
            if (scope.editedFeature) {
              return scope.editedFeature.get("_type");
            }
            if (scope.drawPoint.active) return "point";
            else if (scope.drawLine.active) return "line";
            else if (scope.drawPolygon.active) return "polygon";
            else if (scope.drawText.active) return "text";
          };

          scope.$on("owsContextReseted", function () {
            source.clear();
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnStyleForm
   *
   * @description
   * Form to edit features style. The form content depends on gnStyleType
   * attribute
   */
  module.directive("gnStyleForm", [
    function () {
      return {
        restrict: "A",
        replace: false,
        templateUrl: "../../catalog/components/viewer/draw/" + "partials/styleform.html",
        scope: {
          style: "=gnStyleForm",
          getType: "&gnStyleType"
        },
        link: function (scope, element, attrs) {
          scope.$watch("style.text.width", function (n) {
            scope.style.text.font = n + "px Calibri,sans-serif";
          });

          scope.colors = {
            red: "#FF0000",
            orange: "#FFA500",
            blue: "#0000FF",
            white: "#FFFFFF",
            black: "#000000",
            gray: "#BEBEBE",
            yellow: "#FFFF00",
            green: "#008000",
            pink: "#FFC0CB",
            purple: "#800080",
            brown: "#A52A2A"
          };
        }
      };
    }
  ]);
})();
