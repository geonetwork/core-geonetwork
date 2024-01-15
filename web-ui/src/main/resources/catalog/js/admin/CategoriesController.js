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
  goog.provide("gn_categories_controller");

  var module = angular.module("gn_categories_controller", []);

  /**
   * CategoriesController provides all necessary operations
   * to manage category.
   */
  module.controller("GnCategoriesController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$timeout",
    function ($scope, $routeParams, $http, $rootScope, $translate, $timeout) {
      $scope.categories = null;
      $scope.categorySelected = { id: $routeParams.categoryId };

      $scope.categoryUpdated = false;

      $scope.selectCategory = function (c) {
        $scope.cateroryUpdated = false;
        $scope.categorySelected = c;

        $scope.gnCategoryFrom.$setPristine();

        $timeout(function () {
          $("#categoryname").focus();
        }, 100);
      };

      /**
       * Ask for confirmation to delete the category
       */
      $scope.deleteCategory = function (id) {
        $scope.delCategoryId = id;
        $("#gn-confirm-delete-category").modal("show");
      };

      /**
       * Delete a category
       */
      $scope.confirmDeleteCategory = function (id) {
        $http.delete("../api/tags/" + $scope.delCategoryId).then(
          function (r) {
            if (r.status === 204) {
              $scope.unselectCategory();
              loadCategories();
            } else {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("categoryDeleteError"),
                error: r.data,
                timeout: 0,
                type: "danger"
              });
            }
          },
          function (r) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("categoryDeleteError"),
              error: r.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      /**
       * Save a category
       */
      $scope.saveCategory = function () {
        $http
          .put("../api/tags/" + $scope.categorySelected.id, $scope.categorySelected)
          .then(
            function (response) {
              $scope.unselectCategory();
              loadCategories();
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("categoryUpdated"),
                timeout: 2,
                type: "success"
              });
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("categoryUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      $scope.addCategory = function () {
        $scope.unselectCategory();
        $scope.categorySelected = {
          id: "",
          name: ""
          // label: {
          // TODO: Should define default language
          // based on catalog languages
          // }
        };
        $timeout(function () {
          $("#categoryname").focus();
        }, 100);
      };
      $scope.unselectCategory = function () {
        $scope.categorySelected = {};
      };

      function loadCategories() {
        $http.get("../api/tags").then(function (response) {
          $scope.categories = response.data;
        });
      }
      loadCategories();
    }
  ]);
})();
