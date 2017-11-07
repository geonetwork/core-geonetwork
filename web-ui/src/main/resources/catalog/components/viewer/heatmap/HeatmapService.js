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
  goog.provide('gn_heatmap_service');

  var module = angular.module('gn_heatmap_service', [
  ]);

  module.service('gnHeatmapService', [
    'gnIndexRequestManager',
    '$http',
    function(gnIndexRequestManager, $http) {
      var me = this;
      var CELL_SIZE = 12;     // pixels
      var BUFFER_RATIO = 1.5;

      var indexObject = gnIndexRequestManager.register('WfsFilter', 'heatmap');

      /**
       * This will return a promise which, when resolved, will give an array
       * of ol.Feature objects representing cells
       *
       * @param {string} feature type
       * @param {ol.Map} map
       * @return {Promise}
       */
      this.requestHeatmapData = function(featureType, map) {
        var extent = map.getView().calculateExtent(map.getSize());
        var zoom = map.getView().getZoom();

        // data precision is deduced from current zoom view
        var geohashLength = Math.min(Math.max(Math.ceil((zoom + 1) / 2), 1), 12);

        // viewbox filter
        ol.extent.buffer(extent, BUFFER_RATIO, extent);
        var topLeft = ol.proj.toLonLat(ol.extent.getTopLeft(extent));
        var bottomRight = ol.proj.toLonLat(ol.extent.getBottomRight(extent));

        // cap extent values
        topLeft[0] = Math.min(Math.max(topLeft[0], -180), 180);
        topLeft[1] = Math.min(Math.max(topLeft[1], -90), 90);
        bottomRight[0] = Math.min(Math.max(bottomRight[0], -180), 180);
        bottomRight[1] = Math.min(Math.max(bottomRight[1], -90), 90);

        // trigger search on ES
        return $http.post(indexObject.ES_URL, {
          "query": {
            "bool": {
              "must": [{
                "match_all": {}
              }, {
                "match_phrase": {
                  "featureTypeId": {
                    "query": featureType
                  }
                }
              }, {
                "geo_bounding_box": {
                    "location" : {
                      "top_left" : topLeft,
                      "bottom_right" : bottomRight
                    }
                }
              }]
            }
          },
          "size": 0,
          "aggs": {
            "cells": {
              "geohash_grid": {
                "field": "location",
                "precision": geohashLength
              }
            }
          }
        }).then(function(response) {
          var buckets = response.data.aggregations.cells.buckets;

          // compute cells array based on received data from ES
          return buckets.map(function(cell) {
            var hash = cell.key;
            var bounds = Geohash.bounds(hash);

            // create feature with cell data
            return new ol.Feature({
              geometry: me.buildCellGeometry(
                bounds.sw.lon, bounds.sw.lat, bounds.ne.lon, bounds.ne.lat),
              count: cell.doc_count
            });
          });
        });
      };

      /**
       * Returns a square geometry with the correct center and size.
       *
       * @param {number} minLon
       * @param {number} minLat
       * @param {number} maxLon
       * @param {number} maxLat
       * @return {ol.geom.Polygon}
       */
      this.buildCellGeometry = function(minLon, minLat, maxLon, maxLat) {
        var min = ol.proj.fromLonLat([minLon, minLat]);
        var max = ol.proj.fromLonLat([maxLon, maxLat]);
        return new ol.geom.Polygon([[
          [min[0], min[1]],
          [max[0], min[1]],
          [max[0], max[1]],
          [min[0], max[1]]
        ]], 'XY');
      };

      // this will generate styles with a color gradient
      var startColor = [241, 215, 142];
      var endColor = [250, 150, 150];
      var cellStyles = [];
      var stepCount = 10;
      for (var i = 0; i < stepCount; i++) {
        var ratio = i / (stepCount - 1);
        var c = startColor.map(function(value, index) {
          return value + ratio * (endColor[index] - value);
        });
        var cssFillColor = 'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ', 0.6)';
        var cssColor = 'rgb(' + c[0] + ',' + c[1] + ',' + c[2] + ')';
        cellStyles.push(new ol.style.Style({
          fill: new ol.style.Fill({ color: cssFillColor }),
          stroke: new ol.style.Stroke({
            color: cssColor,
            width: 2
          })
        }));
      }

      // this function returns the correct style according to the 'count'
      // attribute on the feature
      var cellStyleFunction = function(feature) {
        var densityRatio = 0;
        return cellStyles[Math.floor(densityRatio * stepCount)];
      };

      /**
       * Returns the style function to use for cells.
       *
       * @return {ol.style}
       */
      this.getCellStyle = function() {
        return cellStyleFunction;
      };
    }]);
})();
