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
  goog.provide("gn_suggestion_directive");

  /**
   * Provide directives for suggestions of the
   * edited metadata.
   *
   * - gnSuggestionList
   */
  angular
    .module("gn_suggestion_directive", [])
    .directive("gnSuggestionList", [
      "gnSuggestion",
      "gnCurrentEdit",
      "$rootScope",
      "$translate",
      "$interpolate",
      function (gnSuggestion, gnCurrentEdit, $rootScope, $translate, $interpolate) {
        return {
          restrict: "A",
          templateUrl: "../../catalog/components/edit/suggestion/" + "partials/list.html",
          scope: {},
          link: function (scope, element, attrs) {
            scope.gnSuggestion = gnSuggestion;
            scope.gnCurrentEdit = gnCurrentEdit;
            scope.suggestions = [];
            scope.loading = false;

            scope.load = function () {
              scope.loading = true;
              scope.suggestions = [];
              gnSuggestion.load(scope.$parent.lang || "eng").then(
                function (response) {
                  var data = response.data;

                  scope.loading = false;
                  if (data && !angular.isString(data)) {
                    scope.suggestions = data;
                    angular.forEach(scope.suggestions, function (sugg) {
                      var value = sugg.name;
                      sugg.name = $interpolate(value)(scope.$parent);
                    });
                  } else {
                    scope.suggestions = [];
                  }
                },
                function (response) {
                  scope.loading = false;
                  $rootScope.$broadcast("StatusUpdated", {
                    title: $translate.instant("suggestionListError"),
                    error: response.data,
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
            };

            // Reload suggestions list when a directive requires it
            scope.$watch("gnSuggestion.reload", function () {
              if (scope.gnSuggestion.reload) {
                scope.load();
                scope.gnSuggestion.reload = false;
              }
            });

            // When saving is done, refresh validation report
            // scope.$watch('gnCurrentEdit.saving', function(newValue) {
            //   if (newValue === false) {
            //     scope.load();
            //   }
            // });
          }
        };
      }
    ])
    .directive("gnSuggestButton", [
      "gnEditor",
      "gnSuggestion",
      function (gnEditor, gnSuggestion) {
        return {
          restrict: "A",
          replace: true,
          scope: {
            processId: "@gnSuggestButton",
            params: "@",
            name: "@",
            help: "@",
            icon: "@",
            target: "@?"
          },
          templateUrl:
            "../../catalog/components/edit/suggestion/partials/suggestbutton.html",
          link: function (scope, element, attrs) {
            scope.sugg = undefined;
            scope.gnSuggestion = gnSuggestion;
            gnSuggestion.load(scope.$parent.lang || "eng").then(function (response) {
              var data = response.data;

              if (data && !angular.isString(data)) {
                scope.suggestions = data;
                for (var i = 0; i < data.length; i++) {
                  if (data[i].process === scope.processId) {
                    if (scope.target) {
                      if (data[i].target === scope.target) {
                        scope.sugg = data[i];
                        break;
                      }
                    } else {
                      scope.sugg = data[i];
                      break;
                    }
                  }
                }
              }
            });
          }
        };
      }
    ])
    .directive("gnDataAnalyzerButton", [
      "gnEditor",
      "gnCurrentEdit",
      "$http",
      "gnSuggestion",
      function (gnEditor, gnCurrentEdit, $http, gnSuggestion) {
        return {
          restrict: "A",
          replace: true,
          scope: {
            processId: "@gnDataAnalyzerButton",
            params: "@",
            name: "@",
            datasource: "@",
            help: "@",
            icon: "@",
            target: "@?"
          },
          templateUrl:
            "../../catalog/components/edit/suggestion/partials/dataanalyzerbutton.html",
          link: function (scope, element, attrs) {
            scope.isSupported = function () {
              return (
                scope.datasource.match(
                  /.*\/attachments\/.*(.shp|.csv|.xls|.xlsx|.gpkg|.parquet|.json|.geojson)$/i
                ) !== null
              );
            };
            scope.sugg = undefined;
            scope.gnSuggestion = gnSuggestion;
            scope.info = function () {
              var url =
                "../api/data/" +
                gnCurrentEdit.uuid +
                "/data/analyze?datasource=" +
                scope.datasource.replace(/.*\/attachments\//, "attachments/");
              window.open(url, "_blank");
            };
            scope.preview = function () {
              var url =
                "../api/data/" +
                gnCurrentEdit.uuid +
                "/data/analysis/preview?datasource=" +
                scope.datasource.replace(/.*\/attachments\//, "attachments/");
              window.open(url, "_blank");
            };
            scope.save = function () {
              $http
                .put(
                  "../api/data/" +
                    gnCurrentEdit.uuid +
                    "/data/analysis/apply?datasource=" +
                  scope.datasource.replace(/.*\/attachments\//, "attachments/")
                )
                .then(function (response) {
                  gnEditor.refreshEditorForm();
                });
            };
          }
        };
      }
    ])
    .directive("gnRunSuggestion", [
      "gnSuggestion",
      "$interpolate",
      function (gnSuggestion, $interpolate) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/edit/suggestion/partials/runprocess.html",
          link: function (scope, element, attrs) {
            scope.gnSuggestion = gnSuggestion;
            // Indicate if processing is running
            scope.processing = false;
            // Indicate if one process is complete
            scope.processed = false;
            /**
             * Init form parameters.
             * This function is registered to be called on each
             * suggestion click in the suggestions list.
             */
            var initParams = function () {
              scope.params = {};
              scope.currentSuggestion = gnSuggestion.getCurrent();
              scope.processParams = angular.fromJson(scope.currentSuggestion.params);
              for (var key in scope.processParams) {
                if (scope.processParams[key].type == "expression") {
                  scope.params[key] = $interpolate(scope.processParams[key].defaultValue)(
                    scope
                  );
                } else {
                  scope.params[key] = scope.processParams[key].defaultValue;
                }
              }
            };

            scope.runProcess = function () {
              scope.processing = true;
              gnSuggestion
                .runProcess(gnSuggestion.getCurrent()["process"], scope.params)
                .then(function () {
                  scope.processing = false;
                  scope.processed = true;
                  if (angular.isDefined(attrs["id"])) {
                    $("#" + attrs["id"] + "-popup").modal("hide");
                  }
                });
            };
            gnSuggestion.register(initParams);
          }
        };
      }
    ]);
})();
