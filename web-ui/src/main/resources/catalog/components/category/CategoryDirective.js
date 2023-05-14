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
  goog.provide("gn_category_directive");

  var module = angular.module("gn_category_directive", []);

  /**
   * Provide a list of categories if at least one
   * exist in the catalog
   *
   */
  module.directive("gnCategory", [
    "$http",
    "$translate",
    function ($http, $translate) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        scope: {
          element: "=gnCategory",
          lang: "@lang",
          label: "@label"
        },
        templateUrl: "../../catalog/components/category/partials/" + "category.html",
        link: function (scope, element, attrs) {
          $http.get("../api/tags", { cache: true }).then(function (response) {
            scope.categories = response.data;
          });

          scope.sortByLabel = function (c) {
            return c.label[scope.lang];
          };
        }
      };
    }
  ]);

  module.directive("gnBatchCategories", [
    "gnUtilityService",
    "$http",
    "$translate",
    "$q",
    function (gnUtilityService, $http, $translate, $q) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        templateUrl: "../../catalog/components/category/partials/" + "batchcategory.html",
        link: function (scope, element, attrs) {
          scope.report = null;
          scope.categoryIsSelected = false;

          scope.selectCategory = function () {
            scope.categoryIsSelected = true;
          };

          $http.get("../api/tags", { cache: true }).then(function (response) {
            scope.categories = response.data;
          });

          scope.reset = function () {
            element.find("input.ng-dirty").each(function (idx, el) {
              el.checked = false;
            });
            scope.catsForm.$setPristine();
            scope.catsForm.$setUntouched();
            scope.categoryIsSelected = false;
          };

          scope.save = function (replace) {
            scope.report = null;
            var defer = $q.defer(),
              tagsToAdd = [],
              tagsToRemove = [],
              url =
                "../api/records/tags?" +
                "&bucket=" +
                (attrs.selectionBucket || "metadata") +
                "&" +
                (replace ? "clear=true&" : "");
            angular.forEach(scope.categories, function (c) {
              if (c.checked === true) {
                tagsToAdd.push(c.id);
              }
            });
            if (tagsToAdd.length > 0) {
              url = url + "&id=" + tagsToAdd.join("&id=");
            }

            element.find("input.ng-dirty").each(function (c, el) {
              if (el.checked === false) {
                tagsToRemove.push($(el).attr("name"));
              }
            });
            if (tagsToRemove.length > 0) {
              url = url + "&removeId=" + tagsToRemove.join("&removeId=");
            }

            $http.put(url).then(
              function (response) {
                var data = response.data;

                scope.processReport = data;

                gnUtilityService.openModal(
                  {
                    title: $translate.instant("categoriesUpdated"),
                    content: '<div gn-batch-report="processReport"></div>',
                    className: "gn-category-popup",
                    onCloseCallback: function () {
                      scope.processReport = null;
                    }
                  },
                  scope,
                  "CategoryUpdated"
                );

                scope.report = data;
                defer.resolve(data);
              },
              function (response) {
                scope.processReport = response.data;

                gnUtilityService.openModal(
                  {
                    title: $translate.instant("categoriesUpdated"),
                    content: '<div gn-batch-report="processReport"></div>',
                    className: "gn-category-popup",
                    onCloseCallback: function () {
                      scope.processReport = null;
                    }
                  },
                  scope,
                  "CategoryUpdated"
                );

                defer.reject(data);
              }
            );
            return defer.promise;
          };
        }
      };
    }
  ]);
})();
