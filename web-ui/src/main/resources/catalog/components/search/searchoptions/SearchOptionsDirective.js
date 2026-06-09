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
  goog.provide("gn_searchoptions_directive");

  var module = angular.module("gn_searchoptions_directive", []);

  module.directive("gnSearchOptions", [
    "$rootScope",
    "$http",
    "gnGlobalSettings",
    "gnSearchLocation",
    function ($rootScope, $http, gnGlobalSettings, gnSearchLocation) {
      return {
        restrict: "E",
        require: "^ngSearchForm",
        scope: {},
        templateUrl:
          "../../catalog/components/search/searchoptions/partials/" +
          "searchoptions.html",
        link: function (scope, element, attrs, controller) {
          scope.user = $rootScope.user;
          scope.optionsConfig = gnGlobalSettings.gnCfg.mods.search.searchOptions;
          scope.init = function () {
            if (gnSearchLocation.isEditorBoard()) {
              scope.onlyMyRecord = gnGlobalSettings.gnCfg.mods.editor.isUserRecordsOnly;
            } else {
              // For the search page, display the option, but not checked by default
              scope.onlyMyRecord = false;
            }
            scope.languageStrategy =
              controller.getLanguageStrategy() ||
              gnGlobalSettings.gnCfg.mods.search.languageStrategy;
            scope.forcedLanguage = controller.getForcedLanguage();
            scope.languagesAvailable = [];
            scope.languagesStats = {};

            var configWhiteList = gnGlobalSettings.gnCfg.mods.search.languageWhitelist;
            if (configWhiteList && configWhiteList.length > 0) {
              scope.languagesAvailable = configWhiteList;
              controller.setLanguageWhiteList(configWhiteList);
            } else {
              $http
                .post("../api/search/records/_search", {
                  size: 0,
                  query: {
                    terms: { isTemplate: ["n"] }
                  },
                  aggregations: {
                    mainLanguage: {
                      terms: {
                        field: "mainLanguage",
                        size: 10,
                        exclude: ""
                      }
                    },
                    otherLanguage: {
                      terms: {
                        field: "otherLanguage",
                        size: 10,
                        exclude: ""
                      }
                    }
                  }
                })
                .then(function (response) {
                  var data = response.data;
                  angular.forEach(data.aggregations.mainLanguage.buckets, function (i) {
                    scope.languagesStats[i.key] = i.doc_count;
                  });
                  angular.forEach(data.aggregations.otherLanguage.buckets, function (i) {
                    scope.languagesStats[i.key] = i.doc_count;
                  });
                  scope.languagesAvailable = Object.keys(scope.languagesStats);
                  controller.setLanguageWhiteList(scope.languagesAvailable);
                });
            }
          };

          // this enables to keep the dropdown active when we click on the label
          element.find("label > span").each(function (i, e) {
            $(e).on("click", function () {
              $(e).parent().find("input").focus();
            });
          });
          Object.defineProperty(scope, "exactMatch", {
            get: function () {
              return controller.getExactMatch();
            },
            set: function (value) {
              controller.setExactMatch(value);
            }
          });

          Object.defineProperty(scope, "titleOnly", {
            get: function () {
              return controller.getTitleOnly();
            },
            set: function (value) {
              controller.setTitleOnly(value);
            }
          });

          Object.defineProperty(scope, "languageStrategy", {
            get: function () {
              return controller.getLanguageStrategy();
            },
            set: function (value) {
              controller.setLanguageStrategy(value);
            }
          });

          Object.defineProperty(scope, "forcedLanguage", {
            get: function () {
              return controller.getForcedLanguage();
            },
            set: function (value) {
              controller.setForcedLanguage(value);
            }
          });

          Object.defineProperty(scope, "languageWhiteList", {
            get: function () {
              return controller.getLanguageWhiteList();
            },
            set: function (value) {
              controller.setLanguageWhiteList(value);
            }
          });

          Object.defineProperty(scope, "onlyMyRecord", {
            get: function () {
              return controller.getOnlyMyRecord();
            },
            set: function (value) {
              controller.setOnlyMyRecord(value);
            }
          });

          scope.$watch("languageStrategy", function (n, o) {
            if (o === "forceALanguage") {
              controller.setForcedLanguage(undefined);
            }
          });
          scope.init();
        }
      };
    }
  ]);
})();
