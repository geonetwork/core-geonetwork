/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
  goog.provide("gn_search_manager_service");

  var module = angular.module("gn_search_manager_service", []);

  module.factory("gnSearchManagerService", [
    "gnUtilityService",
    "$q",
    "$rootScope",
    "$http",
    "gnHttp",
    function (gnUtilityService, $q, $rootScope, $http, gnHttp) {
      /**
       * Utility to format a search response. JSON response
       * when containing one element will not make an array.
       * Tidy the JSON to be always the same if one or more
       * elements.
       */
      var format = function (data) {
        // Retrieve facet and add name as property and remove @count
        var facets = {},
          dimension = [],
          results = -1,
          listOfArrayFields = [
            "image",
            "link",
            "format",
            "keyword",
            "otherConstr",
            "Constraints",
            "SecurityConstraints"
          ];

        // When using summaryOnly=true, the facet is the root element
        if (data[0] && data[0]["@count"]) {
          data.summary = data[0];
          results = data[0]["@count"];
        }

        // Cleaning facets
        for (var facet in data.summary) {
          if (facet == "dimension") {
            dimension = gnUtilityService.traverse(
              data.summary.dimension,
              gnUtilityService.formatObjectPropertyAsArray,
              "category"
            );
          } else if (facet != "@count" && facet != "@type") {
            facets[facet] = data.summary[facet];
            facets[facet].name = facet;
          } else if (facet == "@count") {
            // Number of results
            results = data.summary[facet];
          }
        }

        if (data.metadata) {
          // Retrieve metadata
          for (
            var i = 0;
            i < data.metadata.length || (!$.isArray(data.metadata) && i < 1);
            i++
          ) {
            var metadata = $.isArray(data.metadata) ? data.metadata[i] : data.metadata;

            // Fix all fields which are arrays and are returned as string
            // when only one value returned.
            for (var property in metadata) {
              if (
                metadata.hasOwnProperty(property) &&
                listOfArrayFields.indexOf(property) != -1 &&
                typeof metadata[property] === "string"
              ) {
                metadata[property] = [metadata[property]];
              }
            }

            // Parse selected to boolean
            metadata.selected = metadata.selected == "true";
          }
        }

        var records = [];
        if (data.metadata && data.metadata.length) {
          records = data.metadata; // results is an array
        } else if (data.metadata) {
          records = [data.metadata]; // only one result
        }

        return {
          facet: facets,
          dimension: dimension,
          count: results,
          metadata: records
        };
      };

      var gnSearch = function (params, error, internal) {
        var defer = $q.defer();

        gnHttp.callService(internal ? "internalSearch" : "search", params).then(
          function (response) {
            defer.resolve(format(response.data));
          },
          function (response) {
            defer.reject(response.data);
          }
        );
        return defer.promise;
      };
      var selected = function (bucket) {
        return $http.get("../api/selections/" + (bucket || "metadata"));
      };
      var select = function (uuid, bucket) {
        return $http.put("../api/selections/" + (bucket || "metadata"), null, {
          params: {
            uuid: uuid
          }
        });
      };
      var unselect = function (uuid, bucket) {
        return $http.delete("../api/selections/" + (bucket || "metadata"), {
          params: {
            uuid: uuid
          }
        });
      };
      var selectAll = function (bucket) {
        return $http.put("../api/selections/" + (bucket || "metadata"));
      };
      var selectNone = function (bucket) {
        return $http.delete("../api/selections/" + (bucket || "metadata"));
      };

      var selectionStatistics = function (field, bucket) {
        return $http.get(
          "../api/selections/" + (bucket || "metadata") + "/statistics?field=" + field
        );
      };

      return {
        format: format,
        gnSearch: gnSearch,
        selected: selected,
        select: select,
        unselect: unselect,
        selectAll: selectAll,
        selectNone: selectNone,
        selectionStatistics: selectionStatistics
      };
    }
  ]);
})();
