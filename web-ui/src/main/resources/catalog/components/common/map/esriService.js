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
  goog.provide('gn_esri_service');

  var module = angular.module('gn_esri_service', []);

  var PADDING = 5;
  var FONT_SIZE = 12;
  var TMP_IMAGE = new Image();

  module.service('gnEsriUtils', ['$q',
    function($q) {
      return {
        /**
         * Renders a JSON legend asynchronously to an image
         * @param {Object} json
         * @return {Promise<string>} data url
         */
        renderLegend(json) {
          var $this = this;

          var canvas = document.createElement('canvas');
          canvas.width = 200;
          canvas.height = 500;
          var context = canvas.getContext('2d');
          context.textBaseline = 'middle';
          var promise = $q.resolve(0);

          // chain one promise per legend
          for (var i = 0; i < json.layers.length; i++) {
            var layer = json.layers[i];
            promise = promise.then(function (y) {
              var layer = this;
              y += PADDING;
              context.font = 'bold ' + FONT_SIZE + 'px sans-serif';
              context.fillText(layer.layerName, PADDING, y + PADDING + FONT_SIZE / 2);
              y += PADDING + FONT_SIZE;
              return $this.renderRules(y, context, layer.legend);
            }.bind(layer));
          }

          return promise.then(function() {
            return canvas.toDataURL('image/png');
          });
        },

        /**
         * Renders a array of rules asynchronously
         * @param {number} currentY
         * @param {CanvasRenderingContext2D} context
         * @param {Object[]} rules
         * @return {Promise<number>} current y
         */
        renderRules(currentY, context, rules) {
          var $this = this;
          var promise = $q.resolve(currentY);

          // chain one promise for each rule
          for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            promise = promise.then(function (y) {
              var rule = this;
              return $this.renderImageData(rule.imageData, rule.contentType).then(function (image) {
                context.drawImage(image, PADDING, y + PADDING);
                context.fillStyle = 'black';
                context.font = FONT_SIZE + 'px sans-serif';
                context.fillText(rule.label, PADDING * 2 + image.width, y + PADDING + image.height / 2);
                y += image.height + PADDING;
                return y;
              })
            }.bind(rule));
          }

          return promise;
        },

        /**
         * Returns a promise resolving on an Image element
         * with the data loaded
         * @param {string} imageData base-64 encoded image data
         * @param {string} format, defaults to 'image/png'
         * @return {Promise<Image>} image
         */
        renderImageData(imageData, format) {
          var defer = $q.defer();
          TMP_IMAGE.onload = function() {
            defer.resolve(this);
          };
          TMP_IMAGE.src = 'data:' + (format || 'image/png') + ';base64,' + imageData;
          return defer.promise;
        }
      }
    }
  ]);
})();
