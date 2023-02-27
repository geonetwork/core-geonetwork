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
  goog.provide("gn_usersearches_service");

  var module = angular.module("gn_usersearches_service", []);

  module.service("gnUserSearchesService", [
    "$http",
    "$q",
    "gnESClient",
    function ($http, $q, gnESClient) {
      this.gnESClient = gnESClient;

      this.loadUserSelectionPaged = function (selectionId, pageNumber, pageSize) {
        var t = 0;
        var url = this.gnESClient.getUrl("_search");
        var from = pageNumber * pageSize;
        var size =  pageSize;
        var body =
          "{\n" +
          '  "from": ' +
          from +
          ",\n" +
          '  "size": ' +
          size +
          ",\n" +
          '  "sort": [\n' +
          '    "_id"\n' +
          "  ],\n" +
          '  "query": {\n' +
          '    "function_score": {\n' +
          '      "boost": "5",\n' +
          '      "functions": [\n' +
          "        {\n" +
          '          "filter": {\n' +
          '            "match": {\n' +
          '              "resourceType": "series"\n' +
          "            }\n" +
          "          },\n" +
          '          "weight": 1.5\n' +
          "        },\n" +
          "        {\n" +
          '          "filter": {\n' +
          '            "exists": {\n' +
          '              "field": "parentUuid"\n' +
          "            }\n" +
          "          },\n" +
          '          "weight": 0.3\n' +
          "        },\n" +
          "        {\n" +
          '          "filter": {\n' +
          '            "match": {\n' +
          '              "cl_status.key": "obsolete"\n' +
          "            }\n" +
          "          },\n" +
          '          "weight": 0.2\n' +
          "        },\n" +
          "        {\n" +
          '          "filter": {\n' +
          '            "match": {\n' +
          '              "cl_status.key": "superseded"\n' +
          "            }\n" +
          "          },\n" +
          '          "weight": 0.3\n' +
          "        },\n" +
          "        {\n" +
          '          "gauss": {\n' +
          '            "dateStamp": {\n' +
          '              "scale": "365d",\n' +
          '              "offset": "90d",\n' +
          '              "decay": 0.5\n' +
          "            }\n" +
          "          }\n" +
          "        }\n" +
          "      ],\n" +
          '      "score_mode": "multiply",\n' +
          '      "query": {\n' +
          '        "bool": {\n' +
          '          "must": [\n' +
          "             \n" +
          "            {\n" +
          '              "terms": {\n' +
          '                "userselection": [\n' +
          selectionId +
          "                ]\n" +
          "              }\n" +
          "            }\n" +
          "          ]\n" +
          "        }\n" +
          "      }\n" +
          "    }\n" +
          "  },\n" +
          "   \n" +
          '  "_source": {\n' +
          '    "includes": [\n' +
          '      "uuid",\n' +
          '      "id",\n' +
          '      "creat*",\n' +
          '      "group*",\n' +
          '      "logo",\n' +
          '      "category",\n' +
          '      "cl_topic*",\n' +
          '      "inspire*",\n' +
          '      "resource*",\n' +
          '      "draft*",\n' +
          '      "overview.*",\n' +
          '      "owner*",\n' +
          '      "link*",\n' +
          '      "image*",\n' +
          '      "status*",\n' +
          '      "rating",\n' +
          '      "tag*",\n' +
          '      "geom",\n' +
          '      "contact*",\n' +
          '      "*Org*",\n' +
          '      "hasBoundingPolygon",\n' +
          '      "isTemplate",\n' +
          '      "valid",\n' +
          '      "isHarvested",\n' +
          '      "dateStamp",\n' +
          '      "documentStandard",\n' +
          '      "standardNameObject.default",\n' +
          '      "cl_status*",\n' +
          '      "mdStatus*",\n' +
          '      "recordLink"\n' +
          "    ]\n" +
          "  },\n" +
          '  "track_total_hits": true\n' +
          "}";
        return $http.post(url, body);
        // gnESClient.
      };

      this.updateFavourites = function (id,name,toDelete) {
        var t=0;

        var body = "name="+name+"&action=remove";
        if (toDelete.length>0) {
          body += "&metadataUuids=";
          body+= toDelete.join("&metadataUuids=");
        }

        return $http.put("../api/userselection/" + id , body, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };

      this.loadFeaturedUserSearches = function (type, withPortal) {
        var deferred = $q.defer(),
          usersearches = $http.get("../api/usersearches/featured?type=" + type);
        apiCalls = [usersearches];
        if (withPortal) {
          apiCalls.push($http.get("../api/sources/subportal"));
        }
        $q.all(apiCalls).then(function (alldata) {
          var usersearches = [];
          usersearches = usersearches.concat(alldata[0].data);
          if (alldata[1]) {
            deferred.resolve({
              data: usersearches.concat(
                alldata[1].data
                  .filter(function (p) {
                    return p.filter != "";
                  })
                  .map(function (p) {
                    return {
                      names: p.label,
                      url: "any=" + encodeURIComponent("q(" + p.filter + ")"),
                      logo: "../../images/harvesting/" + p.logo,
                      featuredType: "p"
                    };
                  })
              )
            });
          } else {
            deferred.resolve({ data: usersearches });
          }
        });
        return deferred.promise;
      };

      this.deleteUserSelection = function (id) {
        return $http.delete("../api/userselection/" + id);
      };

      this.setUserSelectionStatus = function (id, status) {
        return $http.put("../api/userselection/" + id + "/status", "public=" + status, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };

      this.loadUserSelections = function () {
        return $http.get("../api/userselection");
      };

      this.loadUserSearches = function () {
        return $http.get("../api/usersearches");
      };

      this.loadAllUserSearches = function () {
        return $http.get("../api/usersearches/all");
      };

      this.saveUserSearch = function (userSearch) {
        return $http.put("../api/usersearches", userSearch);
      };

      this.removeUserSearch = function (userSearch) {
        return $http.delete("../api/usersearches/" + userSearch.id);
      };
    }
  ]);
})();
