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
  goog.provide('gn_index_service');

  var module = angular.module('gn_index_service', []);


  module.provider('gnIndexService',
      function() {
        this.$get = ['$http',
          function($http) {
            /**
             * Return index query heatmap parameters
             * based on current map extent and map zoom.
             *
             * @param {ol.map} map The OL map
             * @param {string} name  The heatmap name, default 'geom'
             * @param {int} gridlevel Force the gridlevel. It not defined,
             * compute it based on the map zoom.
             *
             * @return {{
             *  [facet.heatmap]: (*|string),
             *  [facet.heatmap.geom]: string,
             *  [facet.heatmap.gridLevel]: (*|string)}}
             */
            function getHeatmapParams(map, name, gridlevel) {
              var extent = map.getView().calculateExtent(
                  map.getSize()
                  );
              extent = ol.proj.transformExtent(
                  extent,
                  map.getView().getProjection(),
                  'EPSG:4326');

              var xmin = Math.max(extent[0], -180).toFixed(5),
                  xmax = Math.min(extent[2], 180).toFixed(5),
                  ymin = Math.max(extent[1], -90).toFixed(5),
                  ymax = Math.min(extent[3], 90).toFixed(5);


              // Compute grid level based on current zoom
              // Zoom goes from 1 to 28
              // GridLevel 1 to 11 but index may return exception
              // if too many cells are requested (depends on extent).
              // Restrict between 3 and 11
              var gridLevel = function(z) {
                if (0 <= z && z <= 2) {return 2;}
                if (2 < z && z <= 5) {return 3;}
                if (5 < z && z <= 7) {return 4;}
                if (7 < z && z <= 10) {return 5;}
                if (10 < z && z <= 12) {return 6;}
                if (12 < z && z <= 14) {return 7;}
                if (14 < z && z <= 18) {return 8;}
                if (18 < z && z <= 20) {return 9;}
                if (20 < z && z <= 24) {return 10;}
                if (24 < z) {return 11;}
                // Depends on distErrPct in geom field configuration
                // TODO: Maybe compute another lower grid level
                // when the following exception occur: Caused by:
                // java.lang.IllegalArgumentException: Too many cells
                // (361 x 434) for level 8 shape
                // Rect(minX=3.49852,maxX=3.62211,minY=40.49707,maxY=40.57137)
              };
              var computedGridLevel = gridLevel(map.getView().getZoom());
              //var computedGridLevel =
              //  (Math.min(11,
              //    Math.max(2,
              //      (map.getView().getZoom() / 2)
              //      // Better resolution but slow
              //      //(map.getView().getZoom() / 2) + 1
              //  ))).toFixed(0);
              //console.log('Zoom: ' + map.getView().getZoom() +
              //  ' Grid: ' + computedGridLevel);

              // TODO ES
              // https://www.elastic.co/guide/en/elasticsearch/reference/current/
              // search-aggregations-bucket-geohashgrid-aggregation.html
              return {
                'facet.heatmap': name || 'geom',
                'facet.heatmap.geom': '["' +
                    xmin + ' ' +
                    ymin + '" TO "' +
                    xmax + ' ' +
                    ymax + '"]',
                'facet.heatmap.gridLevel':
                    gridlevel || computedGridLevel
              };
            };
            /**
             * Convert a heatmap in an array of features.
             *
             * @param {object} heatmap The heatmap object from the response
             * @param {string} proj  The map projection to create feature into.
             * @param {string} asGrid Use a grid instead of points
             * in cell center
             * @return {Array}
             */
            function heatmapToFeatures(heatmap, proj, asGrid) {
              var grid = {}, features = [];
              for (var i = 0; i < heatmap.length; i++) {
                grid[heatmap[i]] = heatmap[i + 1];
                i++;
              }
              if (grid) {
                // The initial outer level is in row order (top-down),
                // then the inner arrays are the columns (left-right).
                // The entire value is null if there is no matching data.
                var rows = grid.counts_ints2D,
                    cellwidth = (grid.maxX - grid.minX) / grid.columns,
                    cellheight = (grid.maxY - grid.minY) / grid.rows,
                    max = 0;
                //console.log(grid.columns + " x " + grid.rows);
                if (rows === null) {
                  console.warn('Empty heatmap returned.');
                  return [];
                }

                for (var i = 0; i < rows.length; i++) {
                  for (var j = 0; rows[i] != null && j < rows[i].length; j++) {
                    max = Math.max(max, rows[i][j]);
                  }
                }

                for (var i = 0; i < rows.length; i++) {
                  // If any array would be all zeros, a null is returned
                  // instead for efficiency reasons.
                  if (!angular.isArray(rows[i])) {
                    continue;
                  }
                  for (var j = 0; j < rows[i].length; j++) {
                    if (rows[i][j] == 0) {
                      continue;
                    }
                    var geom;
                    // TODO: Start of experiment to display grid
                    if (asGrid) {
                      var pt = new ol.geom.Point([
                        grid.minX + cellwidth * j,
                        grid.maxY - cellheight * i]);
                      var ulc = pt.clone();
                      var coords = [ulc.getCoordinates()];
                      pt.translate(0, -cellheight);
                      coords.push(pt.getCoordinates());
                      pt.translate(cellwidth, 0);
                      coords.push(pt.getCoordinates());
                      pt.translate(0, cellheight);
                      coords.push(pt.getCoordinates());
                      coords.push(ulc.getCoordinates());
                      geom = new ol.geom.Polygon([coords]);
                    } else {
                      geom = new ol.geom.Point([
                        grid.minX + cellwidth * j + cellwidth / 2,
                        grid.maxY - cellheight * i - cellheight / 2]);
                    }
                    var value = rows[i][j];
                    var weight = (value / max).toFixed(4);
                    //var weight = 1 - (1 / (1 + value / (1 / max)));
                    var feature = new ol.Feature({
                      geometry: geom.transform(
                          'EPSG:4326',
                          proj),
                      count: value,
                      weight: weight
                    });
                    //console.log(value + " = " + weight);
                    features.push(feature);
                  }
                }
              }
              return features;
            };
            function deleteDocs(filter) {
              return $http.delete(
                  '../api/search/update',
                  // TODO: Migrate to ES
                  {
                    params: {'query': filter}
                  }
              );
            };
            return {
              getHeatmapParams: getHeatmapParams,
              heatmapToFeatures: heatmapToFeatures,
              deleteDocs: deleteDocs
            };
          }];
      });
})();
