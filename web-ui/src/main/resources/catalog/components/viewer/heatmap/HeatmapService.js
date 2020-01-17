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
    '$q',
    function(gnIndexRequestManager, $http, $q) {
      var me = this;
      var CELL_SIZE = 12;     // pixels
      var BUFFER_RATIO = 1;
      var CELL_LOW_COLOR = [255, 241, 92];
      var CELL_HIGH_COLOR = [255, 81, 40];
      var COLOR_STEP_COUNT = 6;
      var CELLS_OPACITY = 0.7;

      var indexObject = gnIndexRequestManager.register('WfsFilter', 'heatmap');

      /**
       * This will return a promise which, when resolved, will give an array
       * of ol.Feature objects representing cells
       *
       * @param {string} feature type
       * @param {ol.Map} map
       * @param {object} query params
       * @param {object} bounding box
       * @param {string} text filter
       * @return {Promise}
       */
      this.requestHeatmapData = function(featureType, map, params, geometry, any) {
        var bufferedSize = map.getSize().map(function(value) {
          return value * BUFFER_RATIO;
        });
        var extent = ol.proj.transformExtent(
          map.getView().calculateExtent(bufferedSize),
          map.getView().getProjection().getCode(),
          "EPSG:4326");
        var zoom = map.getView().getZoom();

        // data precision is deduced from current zoom view
        var geohashLength = 2;
        if (zoom > 3) { geohashLength = 3; }
        if (zoom > 5) { geohashLength = 4; }

        // viewbox filter
        var topLeft = ol.extent.getTopLeft(extent);
        var bottomRight = ol.extent.getBottomRight(extent);

        // cap extent values to world map
        if (bottomRight[0] < topLeft[0]) { bottomRight[0] += 360; }
        var viewWidth = Math.min(360, bottomRight[0] - topLeft[0]);
        topLeft[0] = Math.min(Math.max(topLeft[0], -180), 180 - viewWidth);
        topLeft[1] = Math.min(Math.max(topLeft[1], -90), 90);
        bottomRight[0] = topLeft[0] + viewWidth;
        bottomRight[1] = Math.min(Math.max(bottomRight[1], -90), 90);

        // define base params (without filter)
        var reqParams = {
          query: {
            bool: {
              must: [{
                match_all: {}
              }, {
                match_phrase: {
                  featureTypeId: {
                    query: encodeURIComponent(featureType)
                  }
                }
              }, {
                geo_bounding_box: {
                    location : {
                      top_left : topLeft,
                      bottom_right : bottomRight
                    }
                }
              }]
            }
          },
          size: 0,
          aggs: {
            cells: {
              geohash_grid: {
                field: 'location',
                precision: geohashLength
              }
            }
          }
        };

        // apply filter to params
        // note: merging with the base request is done manually: not ideal but
        // currently no better way available
        var filterParams = indexObject.buildESParams({
          params: params,
          geometry: geometry,
          any: any
        });
        Array.prototype.push.apply(reqParams.query.bool.must,
          filterParams.query.bool.must);
        if (geometry) {
          reqParams.query.bool.filter = filterParams.query.bool.filter;
        }

        // cancel previous request
        if (me.requestCanceller) {
          me.requestCanceller.resolve();
        }

        // this promise will be used to cancel the data request
        me.requestCanceller = $q.defer();

        // trigger search on ES
        return $http.post(indexObject.ES_URL, reqParams, {
          timeout: me.requestCanceller.promise
        }).then(function(response) {
          var buckets = response.data.aggregations.cells.buckets;

          // no data with the current filter
          if (!buckets.length) {
            return [];
          }

          // get max cell count
          me.maxCellCount = buckets[0].doc_count;

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
      var cellStyles = [];
      for (var i = 0; i < COLOR_STEP_COUNT; i++) {
        var ratio = i / (COLOR_STEP_COUNT - 1);
        var c = CELL_LOW_COLOR.map(function(value, index) {
          return Math.floor(value + ratio * (CELL_HIGH_COLOR[index] - value));
        });
        var cssFillColor =
          'rgba(' + c[0] + ',' + c[1] + ',' + c[2] + ',' + CELLS_OPACITY + ')';
        cellStyles.push(new ol.style.Style({
          fill: new ol.style.Fill({ color: cssFillColor })
        }));
      }

      // this is for hovered cells
      var hoveredCellStyle = new ol.style.Style({
        fill: new ol.style.Fill({ color: 'rgba(255, 255, 255, 0.2)' }),
        stroke: new ol.style.Stroke({
          color: 'rgba(255, 255, 255, 0.6)',
          width: 3
        })
      });

      // this function returns the correct style according to the 'count'
      // attribute on the feature
      me.maxCellCount = 1;
      var getCellStyleFunction = function(hovered) {
        return function(feature) {
          var densityRatio = (feature.get('count') || 0) / me.maxCellCount;
          var style = cellStyles[Math.floor(densityRatio * (COLOR_STEP_COUNT - 1))];

          // handle hovered case
          if (hovered) {
            return [
              style,
              hoveredCellStyle
            ];
          } else {
            return style;
          }
        };
      };

      /**
       * Returns the style function to use for cells.
       *
       * @return {ol.style}
       */
      this.getCellStyle = function() {
        return getCellStyleFunction();
      };

      /**
       * Returns the style function to use for selected/hovered cells.
       *
       * @return {ol.style}
       */
      this.getCellHoverStyle = function() {
        return getCellStyleFunction(true);
      };

      /**
       * @return {number}
       */
      this.getCellOpacity = function() {
        return CELLS_OPACITY;
      };
    }]);
})();
