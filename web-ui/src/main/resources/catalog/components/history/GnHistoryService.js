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
  goog.provide("gn_history_service");

  var module = angular.module("gn_history_service", []);

  /**
   * Service to deal with record history.
   */
  module.service("gnRecordHistoryService", [
    "$http",
    "$filter",
    function ($http, $filter) {
      this.delete = function (step) {
        return $http.delete(
          "../api/records/" +
            step.metadataId +
            "/status/" +
            step.statusId +
            "." +
            step.userId +
            "." +
            step.dateChange
        );
      };

      this.close = function (step, optionalDateOrNow) {
        return $http.put(
          "../api/records/" +
            step.metadataId +
            "/status/" +
            step.statusId +
            "." +
            step.userId +
            "." +
            step.dateChange +
            "/close?closeDate=" +
            (optionalDateOrNow || moment().format("YYYY-MM-DDTHH:mm:ss"))
        );
      };

      this.restoreHistoryElement = function (step) {
        return $http.post(
          "../api/records/" +
            step.metadataId +
            "/status/" +
            step.statusId +
            "." +
            step.userId +
            "." +
            step.dateChange +
            "/restore"
        );
      };

      function buildFilter(filter) {
        var filters = [];
        angular.forEach(filter.types, function (v, k) {
          if (v) {
            filters.push("type=" + k);
          }
        });
        if (filter.authorFilter && filter.authorFilter.id) {
          filters.push("author=" + filter.authorFilter.id);
        }
        if (filter.ownerFilter && filter.ownerFilter.id) {
          filters.push("owner=" + filter.ownerFilter.id);
        }
        if (filter.recordFilter) {
          filters.push("recordIdentifier=" + filter.recordFilter);
        }
        if (filter.uuid) {
          filters.push("uuid=" + filter.uuid);
        }
        if (filter.statusId) {
          filters.push("statusIds=" + filter.statusId);
        }
        if (filter.dateFromFilter) {
          filters.push(
            "dateFrom=" + $filter("date")(filter.dateFromFilter, "yyyy-MM-dd")
          );
        }
        if (filter.dateToFilter) {
          filters.push("dateTo=" + $filter("date")(filter.dateToFilter, "yyyy-MM-dd"));
        }

        filters.push("from=" + (filter.from || 0));
        filters.push("size=" + (filter.size || 20));

        return filters.length > 0 ? "?" + filters.join("&") : "";
      }

      this.search = function (filter) {
        return $http.get("../api/records/status/search" + buildFilter(filter));
      };
    }
  ]);
})();
