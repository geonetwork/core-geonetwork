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
  goog.provide("gn_access_manager");

  var module = angular.module("gn_access_manager_controller", []);

  module.controller("GnAccessManagerController", [
    "$scope",
    "$http",
    function ($scope, $http) {
      $scope.groups = [];
      $scope.operations = [];
      $scope.params = { any: "", group: "" };
      var defaultGroups = [1];
      var defaultOperations = [0, 1, 2, 5];
      $scope.size = 100;
      $scope.nbOperations = 0;

      var init = function () {
        // Load operations
        $http.get("../api/operations").then(function (r) {
          $scope.operations = r.data;
          angular.forEach($scope.operations, function (o) {
            o.selected = defaultOperations.indexOf(o.id) !== -1;
          });

          // Load groups
          $http.get("../api/groups?withReservedGroup=true").then(function (r) {
            $scope.groups = r.data;
            angular.forEach($scope.groups, function (o) {
              o.selected = defaultGroups.indexOf(o.id) !== -1;
            });
            $scope.buildQuery();
          });
        });
      };

      $scope.more = function () {
        $scope.size += 100;
        $scope.buildQuery();
      };

      $scope.buildQuery = function () {
        var selectedOperations = [];
        var scriptFields = [];
        $scope.results = undefined;
        $scope.selectedGroups = [];
        $scope.columns = [];

        var filter = [];
        if ($scope.params.any != "") {
          filter.push(
            '{"match": {"resourceTitleObject.default": {' +
              '"query": "' +
              $scope.params.any +
              '", ' +
              '"zero_terms_query": "all", "fuzziness": "auto"}}}'
          );
        }
        if ($scope.params.group != "") {
          filter.push('{"term": {"groupPublished": "' + $scope.params.group + '"}}');
          // Add the group filtered to the table column
          for (var i = 0; i < $scope.groups.length; i++) {
            if ($scope.groups[i].name === $scope.params.group) {
              $scope.groups[i].selected = true;
              break;
            }
          }
        }

        for (var i = 0; i < $scope.operations.length; i++) {
          var o = $scope.operations[i];
          if (o.selected === true) {
            selectedOperations.push(o);
          }
        }

        $scope.nbOperations = selectedOperations.length;

        for (var i = 0; i < $scope.groups.length; i++) {
          var g = $scope.groups[i];
          if (g.selected === true) {
            $scope.selectedGroups.push(g);
            for (var j = 0; j < selectedOperations.length; j++) {
              var o = selectedOperations[j];
              var fieldName = g.name + "-" + o.name;
              scriptFields.push(
                '"' +
                  fieldName +
                  '": {' +
                  '      "script": {' +
                  '        "inline": "doc[\'op' +
                  o.id +
                  "'].size() > 0 && doc['op" +
                  o.id +
                  "'].contains('" +
                  g.id +
                  "')\"" +
                  "      }" +
                  "    }"
              );
              $scope.columns.push({
                group: g,
                operation: o,
                fieldName: fieldName
              });
            }
          }
        }

        var query =
          "{" +
          '  "sort" : [{"resourceTitleObject.default.sort": "asc"}],' +
          '  "query": {' +
          '    "bool": {' +
          '      "must": [' +
          (filter.length > 0 ? filter.join(",") : '{"match_all": {}}') +
          "      ]" +
          "    }" +
          "  }," +
          '  "from": 0,' +
          '  "size": ' +
          $scope.size +
          "," +
          '  "_source": ["resourceTitleObject.default", "uuid"], ' +
          '  "script_fields": {' +
          scriptFields.join(",") +
          "  }" +
          "}";

        $http
          .post("../api/search/records/_search", query, {
            headers: { "Content-type": "application/json" }
          })
          .then(
            function (r) {
              $scope.results = r.data;
            },
            function (r) {
              // TODO report ES error message globally
              $scope.hasSearchError = true;
            }
          );
      };

      $scope.$watchCollection("params", function (n, o) {
        if (n != o) {
          $scope.buildQuery();
        }
      });
      init();
    }
  ]);
})();
