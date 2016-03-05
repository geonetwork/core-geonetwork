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
  goog.provide('gn_solr_service');

  var module = angular.module('gn_solr_service', []);


  module.provider('gnSolrService',
      function() {
        this.$get = ['$http',
          function($http) {
            /**
             * Return Solr query heatmap parameters
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

              return {
                'facet.heatmap': name || 'geom',
                'facet.heatmap.geom': '["' +
                    xmin + ' ' +
                    ymin + '" TO "' +
                    xmax + ' ' +
                    ymax + '"]',
                'facet.heatmap.gridLevel':
                    gridlevel ||
                    // Compute grid level based on current zoom
                    Math.max(3, map.getView().getZoom() / 2).toFixed(0)
              };
            };
            /**
             * Convert a Solr heatmap in an array of features.
             *
             * @param {object} heatmap The heatmap object from the Solr response
             * @param {string} proj  The map projection to create feature into.
             * @return {Array}
             */
            function heatmapToFeatures(heatmap, proj) {
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
                    xcell = (grid.maxX - grid.minX) / grid.columns,
                    ycell = (grid.maxY - grid.minY) / grid.rows,
                    max = 0;

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
                    var point = new ol.geom.Point([
                      grid.minX + xcell * j + xcell / 2,
                      grid.maxY - ycell * i - ycell / 2]);
                    var value = rows[i][j];
                    var feature = new ol.Feature({
                      geometry: point.transform(
                          'EPSG:4326',
                          proj),
                      count: value,
                      weight: value / max
                    });
                    features.push(feature);
                  }
                }
              }
              return features;
            };
            function deleteDocs(filter){
              return $http.delete(
                '../api/search/update',
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
