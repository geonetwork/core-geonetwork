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
  goog.provide("gn_index_service");

  var module = angular.module("gn_index_service", []);

  module.provider("gnIndexService", function () {
    this.$get = [
      "$http",
      "Metadata",
      function ($http, Metadata) {
        // FIXME: this is not used
        function deleteDocs(filter) {
          return $http.delete(
            "../api/search/update",
            // TODO: Migrate to ES
            {
              params: { query: filter }
            }
          );
        }

        function getDoc(uuid) {
          return $http.post("../api/search/records/_search", {
            query: {
              bool: {
                must: [
                  {
                    match: {
                      uuid: uuid
                    }
                  },
                  { terms: { draft: ["n", "y", "e"] } },
                  { terms: { isTemplate: ["n", "y", "s"] } }
                ]
              }
            }
          });
        }

        function getIndexReport(uuid) {
          return this.getDoc(uuid).then(function (r) {
            if (r.data.hits.total.value !== 0) {
              var metadata = new Metadata(r.data.hits.hits[0]);

              var report = {};
              report.allMessages = [].concat(metadata.indexingErrorMsg || []);

              report.warningMessages = report.allMessages.filter(function (msg) {
                return msg && msg.type === "warning";
              });

              report.errorMessages = report.allMessages.filter(function (msg) {
                return msg && msg.type === "error";
              });

              return report;
            }
            throw new Error("No metadata found for UUID: " + uuid);
          });
        }

        return {
          deleteDocs: deleteDocs,
          getDoc: getDoc,
          getIndexReport: getIndexReport
        };
      }
    ];
  });
})();
