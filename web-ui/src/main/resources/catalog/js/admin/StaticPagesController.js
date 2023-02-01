/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
  goog.provide("gn_static_pages_controller");

  var module = angular.module("gn_static_pages_controller", []);

  module.controller("GnStaticPagesController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "gnUrlUtils",
    function ($scope, $http, $rootScope, $translate, gnUrlUtils) {
      $scope.dbLanguages = [];
      $scope.staticPages = [];
      $scope.staticPageSelected = null;

      function loadStaticPages() {
        $scope.staticPageSelected = null;
        $http.get("../api/pages").then(function (r) {
          $scope.staticPages = r.data;

          $scope.staticPages.forEach(function (p) {
            p.pageId = p.linkText;
            delete p.linkText;
          });
        });
      }

      function loadDbLanguages() {
        $http.get("../api/languages").then(function (r) {
          $scope.dbLanguages = r.data;
          $http.get("../api/languages/application").then(function (r) {
            $scope.applicationLanguagesNotAlreadyAvailable = r.data.filter(function (l) {
              return (
                $scope.dbLanguages.find(function (dbL) {
                  return dbL.id === l.id;
                }) === undefined
              );
            });
          });
        });
      }

      $scope.addStaticPage = function () {
        $scope.isUpdate = false;
        $scope.staticPageSelected = {
          language: "",
          pageId: "",
          format: "",
          link: "",
          status: "HIDDEN",
          sections: []
        };
      };

      $scope.selectStaticPage = function (v) {
        $scope.isUpdate = true;
        $scope.staticPageSelected = v;
      };

      $scope.saveStaticPage = function () {
        var sp = Object.assign({}, $scope.staticPageSelected);
        delete sp.$$hashKey;

        if ($scope.isUpdate) {
          sp.newLanguage = sp.language;
          sp.newPageId = sp.pageId;
        }

        $http
          .post(
            "../api/pages" +
              ($scope.isUpdate
                ? "/" +
                  $scope.staticPageSelected.language +
                  "/" +
                  $scope.staticPageSelected.pageId
                : "") +
              "?" +
              gnUrlUtils.toKeyValue(sp)
          )
          .then(
            function (response) {
              loadStaticPages();
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("staticPageUpdated"),
                timeout: 2,
                type: "success"
              });
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("staticPageUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      $scope.deleteStaticPageConfig = function () {
        $("#gn-confirm-remove-static-page").modal("show");
      };

      $scope.confirmDeleteStaticPageConfig = function () {
        $http
          .delete(
            "../api/pages/" +
              $scope.staticPageSelected.language +
              "/" +
              $scope.staticPageSelected.pageId
          )
          .then(
            function () {
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("staticPageRemoved"),
                timeout: 2,
                type: "success"
              });

              loadStaticPages();
            },
            function (data) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("staticPageDeleteError"),
                error: data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      loadDbLanguages();
      loadStaticPages();
    }
  ]);
})();
