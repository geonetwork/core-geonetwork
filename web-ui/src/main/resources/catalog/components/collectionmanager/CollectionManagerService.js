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
  goog.provide("gn_collection_manager_service");

  var module = angular.module("gn_collection_manager_service", [
    "gn_collection_manager_service"
  ]);

  module.service("gnCollectionService", [
    "$http",
    "$q",
    "Metadata",
    "gnMetadataManager",
    function ($http, $q, Metadata, gnMetadataManager) {
      return {
        getTemplates: function (filter) {
          var defer = $q.defer();
          $http
            .post(
              "../api/search/records/_search",
              {
                query: {
                  bool: {
                    must: [
                      { terms: { isTemplate: ["y"] } },
                      { query_string: { query: filter } }
                    ]
                  }
                }
              },
              { cache: true }
            )
            .then(
              function (r) {
                var collectionTemplates = [];
                if (r.data.hits.total.value > 0) {
                  collectionTemplates = r.data.hits.hits.map(function (r) {
                    return new Metadata(r);
                  });
                }
                defer.resolve(collectionTemplates);
              },
              function (r) {
                defer.reject();
              }
            );
          return defer.promise;
        },
        createCollection: function (uuid, memberUuids) {
          var defer = $q.defer();
          var id = undefined;
          $http
            .get("../api/groups?profile=Editor", { cache: true })
            .then(function (response) {
              var data = response.data;

              var groups = data,
                ownerGroup = null;
              if (ownerGroup === null && data) {
                ownerGroup = data[0]["id"];
              }
              gnMetadataManager.copy(uuid, ownerGroup, false, "METADATA").then(
                function (r) {
                  id = r.data;
                  $http
                    .post(
                      "../api/records/" +
                        id +
                        "/processes/collection-updater?" +
                        "newProductMemberUuids=" +
                        memberUuids.join(",")
                    )
                    .then(
                      function () {
                        r.id = id;
                        defer.resolve(r);
                      },
                      function (r) {
                        r.id = id;
                        defer.reject(r);
                      }
                    );
                },
                function (r) {
                  defer.reject(r);
                }
              );
            });

          return defer.promise;
        }
      };
    }
  ]);
})();
