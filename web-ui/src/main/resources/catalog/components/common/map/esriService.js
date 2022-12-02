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
  goog.provide("gn_esri_service");

  var module = angular.module("gn_esri_service", []);

  var PADDING = 5;
  var TITLE_PADDING = 15;
  var FONT_SIZE = 12;

  var TITLE_FONT = "bold " + FONT_SIZE + "px sans-serif";
  var LABEL_FONT = FONT_SIZE + "px sans-serif";

  module.service("gnEsriUtils", [
    "$q",
    "$http",
    "$translate",
    "gnUrlUtils",
    function ($q, $http, $translate, gnUrlUtils) {
      return {
        /**
         * Renders a JSON legend asynchronously to an image
         * @param {Object} json
         * @param {string} [layerId] optional, legend will be filtered on this layer
         * @return {Promise<string>} data url
         */
        renderLegend: function (json, layerId) {
          var singleLayer = !!layerId;
          var legend = singleLayer
            ? {
                layers: json.layers.filter(function (layer) {
                  return layer.layerId == layerId;
                })
              }
            : json;

          var canvas = document.createElement("canvas");
          var context = canvas.getContext("2d");
          context.textBaseline = "middle";
          var size = this.measureLegend(context, legend, singleLayer);

          // size canvas & draw background
          canvas.width = size[0];
          canvas.height = size[1];
          context.fillStyle = "white";
          context.fillRect(0, 0, size[0], size[1]);

          var promises = [];
          var y = 0;

          // chain one promise per legend
          for (var i = 0; i < legend.layers.length; i++) {
            var layer = json.layers[i];
            if (!singleLayer) {
              y += TITLE_PADDING;
              context.fillStyle = "black";
              context.textBaseline = "middle";
              context.font = TITLE_FONT;
              context.fillText(layer.layerName, PADDING, y + FONT_SIZE / 2);
              y += FONT_SIZE;
            }

            promises.push(this.renderRules(y, context, layer.legend));
            y += (layer.legend[0].height + PADDING) * layer.legend.length;
          }

          return $q.all(promises).then(function () {
            return canvas.toDataURL("image/png");
          });
        },

        /**
         * Renders a array of rules asynchronously
         * @param {number} startY
         * @param {CanvasRenderingContext2D} context
         * @param {Object[]} rules
         * @return {Promise<number>} current y
         */
        renderRules: function (startY, context, rules) {
          var promises = [];

          // chain one promise for each rule
          for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            var y = startY + i * (rules[0].height + PADDING) + PADDING;
            promises.push(
              this.renderImageData(rule.imageData, rule.contentType).then(
                function (y, image) {
                  var rule = this;
                  context.drawImage(image, PADDING, y, rule.width, rule.height);
                  context.fillStyle = "black";
                  context.textBaseline = "middle";
                  context.font = LABEL_FONT;
                  context.fillText(
                    rule.label,
                    PADDING * 2 + rule.width,
                    y + rule.height / 2
                  );
                }.bind(rule, y)
              )
            );
          }

          return $q.all(promises);
        },

        /**
         * Returns a promise resolving on an Image element
         * with the data loaded
         * @param {string} imageData base-64 encoded image data
         * @param {string} format, defaults to 'image/png'
         * @return {Promise<Image>} image
         */
        renderImageData: function (imageData, format) {
          var defer = $q.defer();
          var image = new Image();
          image.onload = function () {
            defer.resolve(this);
          };
          image.src = "data:" + (format || "image/png") + ";base64," + imageData;
          return defer.promise;
        },

        /**
         * Returns the expected size of the legend
         * @param {CanvasRenderingContext2D} context
         * @param {Object} json
         * @param {boolean} skipLayerName
         * @return {[number, number]} width and height
         */
        measureLegend: function (context, json, skipLayerName) {
          var width = 100;
          var height = 1;
          for (var i = 0; i < json.layers.length; i++) {
            var layer = json.layers[i];
            if (!skipLayerName) {
              context.font = TITLE_FONT;
              var nameMetrics = context.measureText(layer.layerName);
              width = Math.max(width, nameMetrics.width + PADDING * 2);
              height += TITLE_PADDING + FONT_SIZE;
            }

            for (var j = 0; j < layer.legend.length; j++) {
              var rule = layer.legend[j];
              context.font = LABEL_FONT;
              var ruleMetrics = context.measureText(rule.label);
              width = Math.max(width, rule.width + ruleMetrics.width + PADDING * 3);
              height += PADDING + rule.height;
            }
          }
          return [width, height];
        },

        /**
         * Returns a promise resolving to the capabilities document of an ESRI Rest service.
         * @param {String} url
         * @return {Promise<String>} capabilities document
         */
        getCapabilities: function (url) {
          var timeout = 60 * 1000;
          var defer = $q.defer();

          url = gnUrlUtils.append(
            url,
            gnUrlUtils.toKeyValue({
              f: "json"
            })
          );

          $http
            .get(url, {
              cache: true,
              timeout: timeout
            })
            .then(
              function (response) {
                var data = response.data;

                // Check if the response contains a mapName property,
                // to verify it's an ESRI Rest Capabilities document.
                if (!!data.mapName) {
                  defer.resolve(data);
                } else {
                  defer.reject($translate.instant("esriCapabilitiesNoValid"));
                }
              },
              function (response) {
                defer.reject($translate.instant("esriCapabilitiesFailed"));
              }
            );

          return defer.promise;
        }
      };
    }
  ]);
})();
