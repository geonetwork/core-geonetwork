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
  goog.provide("gn_projection_service");

  var module = angular.module("gn_projection_service", []);

  // constants
  var SERVICE_REST_URL = "https://EPSG.io";
  var REQUEST_TIMEOUT = 60 * 1000;
  var EPSG_LL_WGS84 = "EPSG:4326";
  var EPSG_WEB_MERCATOR = "EPSG:3857";
  var EPSG_REGEX = new RegExp("^EPSG:\\d{4,6}$");

  module.provider("gnProjService", function () {
    this.$get = [
      "$http",
      "$q",
      "$translate",
      "gnUrlUtils",
      function ($http, $q, $translate, gnUrlUtils) {
        var buildRestUrl = function (searchTerm) {
          // Add GET query parameters to serviceUrl
          if (searchTerm.toUpperCase().startsWith("EPSG:")) {
            searchTerm = searchTerm.substring(5);
          }
          return gnUrlUtils.append(
            SERVICE_REST_URL,
            gnUrlUtils.toKeyValue({
              q: searchTerm,
              format: "json"
            })
          );
        };

        var parseProjDef = function (data) {
          var code = "EPSG:";
          var extent = [null, null, null, null];
          var worldExtent = [null, null, null, null];

          // check if there's a useful response
          if (data.status !== "ok" || data.number_result === 0) {
            return {
              label: null,
              code: code,
              def: null,
              extent: extent,
              worldExtent: worldExtent
            };
          }

          // get first result, set EPSG code
          var firstResult = data.results[0];
          code += firstResult.code;

          // register projection so we can transform extent
          var def = firstResult.proj4;
          if (def) {
            proj4.defs(code, firstResult.proj4);
            ol.proj.proj4.register(proj4);
          }

          // get bounding box and define defaults
          var bbox = firstResult.bbox;

          if (bbox && bbox.length === 4) {
            // EPSG.io returns bbox as lat-lon, but we want lon-lat
            worldExtent = [bbox[1], bbox[2], bbox[3], bbox[0]];
            // check if the world extent crosses the dateline
            if (bbox[1] > bbox[3]) {
              worldExtent = [bbox[1], bbox[2], bbox[3] + 360, bbox[0]];
            }
            // transform world extent to projected extent
            extent = ol.proj.transformExtent(worldExtent, EPSG_LL_WGS84, code, 8);
          }

          return {
            label: firstResult.name,
            code: code,
            def: def,
            extent: extent,
            worldExtent: worldExtent
          };
        };

        return {
          helpers: {
            getServiceName: function () {
              // Get API service name without http(s) prefix for display purposes
              return {
                value: SERVICE_REST_URL.substr(SERVICE_REST_URL.indexOf("://") + 3)
              };
            },

            isDefaultProjection: function (code) {
              return code === EPSG_LL_WGS84 || code === EPSG_WEB_MERCATOR;
            },

            isValidEpsgCode: function (code) {
              return code.search(EPSG_REGEX) >= 0;
            }
          },

          getProjectionSettings: function (searchTerm) {
            var defer = $q.defer();
            var url = buildRestUrl(searchTerm);

            // send request and decode result
            if (true) {
              $http
                .get(url, {
                  cache: true,
                  timeout: REQUEST_TIMEOUT
                })
                .then(
                  function (response) {
                    try {
                      defer.resolve(parseProjDef(response.data));
                    } catch (e) {
                      console.error(e);
                      defer.reject($translate.instant("failedToParseProjDefinition"));
                    }
                  },
                  function (response) {
                    defer.reject(
                      $translate.instant(
                        response.status === 401
                          ? "checkProjectionUrlUnauthorized"
                          : "checkProjectionUrl",
                        { url: url, status: response.status }
                      )
                    );
                  }
                );
            }
            return defer.promise;
          }
        };
      }
    ];
  });
})();
