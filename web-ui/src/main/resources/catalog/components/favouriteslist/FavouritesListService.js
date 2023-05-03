/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

/*
 *  This provides some support for the favourites list panel for talking with the backend server.
 */
(function () {
  goog.provide("gn_favouriteslist_service");

  var module = angular.module("gn_favouriteslist_service", []);
  module.service("gnFavouritesListService", [
    "$http",
    "$q",
    "gnESClient",
    "$rootScope",
    "gnUtilityService",
    function ($http, $q, gnESClient, $rootScope, gnUtilityService) {
      this.gnESClient = gnESClient;
      cachedFavouritesLists = [];

      this.loadFavouritesListItemsPaged = function (
        favouritesListId,
        pageNumber,
        pageSize
      ) {
        var url = this.gnESClient.getUrl("_search");
        var from = pageNumber * pageSize;
        var size = pageSize;
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
          '                "favouritesList": [\n' +
          favouritesListId +
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
      };

      this.getCachedLists = function () {
        return cachedFavouritesLists;
      };

      this.addManyToList = function (favouriteList, uuids) {
        var thisService = this;
        if (favouriteList.id > -1) {
          return thisService
            .addToFavorites(favouriteList.id, favouriteList.name, uuids)
            .then(function (response) {
              return thisService.loadFavourites();
            })
            .then(function (result) {
              $rootScope.$emit("favouriteSelectionsUpdate", result.data);
              $rootScope.$emit("refreshFavouriteLists", result.data);
              return result;
            });
        }
      };

      this.addToList = function (favouriteList, uuid) {
        var thisService = this;
        if (favouriteList.id > -1) {
          return thisService
            .addToFavorites(favouriteList.id, favouriteList.name, [uuid])
            .then(function (response) {
              return thisService.loadFavourites();
            })
            .then(function (result) {
              $rootScope.$emit("favouriteSelectionsUpdate", result.data);
              $rootScope.$emit("refreshFavouriteLists", result.data);
              return result;
            });
        }
      };

      // get the favourites lists from server
      // also, caches the results
      this.loadFavourites = function () {
        return $http
          .get("../api/favouriteslist")
          .then(function (result) {
            result.data = _.sortBy(result.data, function (item) {
              return item.name;
            });
            return result;
          })
          .then(function (result) {
            cachedFavouritesLists = result.data;
            return result;
          });
      };

      this.deleteFavouritesList = function (id) {
        return $http.delete("../api/favouriteslist/" + id).then(function (result) {
          cachedFavouritesLists = cachedFavouritesLists.filter(function (item) {
            return item.id !== id;
          });
          return result;
        });
      };

      this.setFavouritesListStatus = function (id, status) {
        return $http
          .put("../api/favouriteslist/" + id + "/status", "public=" + status, {
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
          })
          .then(function (result) {
            cachedFavouritesLists.foreach(function (item) {
              if (item.id === id) {
                item.public = !item.public;
              }
            });

            return result;
          });
      };

      //caller will call loadFavourites()
      this.createFavourites = function (name) {
        var body = "name=" + name + "&listType=PreferredList";

        return $http.post("../api/favouriteslist", body, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };

      //caller will call loadFavourites()
      this.addToFavorites = function (id, name, uuids) {
        var body = "name=" + name + "&action=add";
        if (uuids.length > 0) {
          body += "&metadataUuids=";
          body += uuids.join("&metadataUuids=");
        }

        return $http.put("../api/favouriteslist/" + id, body, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };

      this.getAllSelections = function (bucket) {
        return $http.get("../api/selections/" + bucket);
      };

      this.selectListBulkAdd = function (bucket) {
        $rootScope.selectionBucket = bucket;
        gnUtilityService.openModal(
          {
            title: "chooseFavouritesList",
            content: "<div gn-choose-favourites-list></div>",
            className: "gn-choosefavouriteslist-popup",
            onCloseCallback: function () {
              var t = 0;
            }
          },
          $rootScope,
          "Favourites.bulkAdd"
        );
        var t = 0;
      };

      //also updates name
      //caller will call loadFavourites()
      this.removeFromFavourites = function (id, name, toDelete) {
        var body = "name=" + name + "&action=remove";
        if (toDelete.length > 0) {
          body += "&metadataUuids=";
          body += toDelete.join("&metadataUuids=");
        }
        var thisService = this;

        return $http
          .put("../api/favouriteslist/" + id, body, {
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
          })
          .then(function (response) {
            return thisService.loadFavourites();
          })
          .then(function (result) {
            $rootScope.$emit("favouriteSelectionsUpdate", result.data);
            $rootScope.$emit("refreshFavouriteLists", result.data);
            return result;
          });
      };
    }
  ]);
})();
