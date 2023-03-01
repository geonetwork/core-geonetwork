(function () {
  goog.provide("gn_favouriteslist_service");

  var module = angular.module("gn_favouriteslist_service", []);
  module.service("gnFavouritesListService", [
    "$http",
    "$q",
    "gnESClient",
    function ($http, $q, gnESClient) {
      this.gnESClient = gnESClient;

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

      this.loadFavourites = function () {
        return $http.get("../api/favouriteslist");
      };

      this.deleteFavouritesList = function (id) {
        return $http.delete("../api/favouriteslist/" + id);
      };

      this.setFavouritesListStatus = function (id, status) {
        return $http.put("../api/favouriteslist/" + id + "/status", "public=" + status, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };

      this.updateFavourites = function (id, name, toDelete) {
        var body = "name=" + name + "&action=remove";
        if (toDelete.length > 0) {
          body += "&metadataUuids=";
          body += toDelete.join("&metadataUuids=");
        }

        return $http.put("../api/favouriteslist/" + id, body, {
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });
      };
    }
  ]);
})();
