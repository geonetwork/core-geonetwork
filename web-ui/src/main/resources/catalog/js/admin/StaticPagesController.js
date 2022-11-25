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
      $scope.staticPages = [];
      $scope.staticPageSelected = null;

      function loadStaticPages() {
        $scope.staticPageSelected = null;
        $http.get("../api/pages/list").success(function (data) {
          $scope.staticPages = data;

          $scope.staticPages.forEach(function (p) {
            // Support 1 section. TODO: Support multiple sections
            p.section = _.first(_.filter(p.sections, function(s) { return s !== 'DRAFT'}));
            p.pageId = p.linkText;
            delete p.linkText;
            delete p.sections;
          })
        });
      }

      $scope.addStaticPage = function () {
        $scope.isUpdate = false;
        $scope.staticPageSelected = {
          language: "",
          pageId: "",
          format: "",
          link: "",
          status: "HIDDEN"
        };
      }

      $scope.selectStaticPage = function (v) {
        $scope.isUpdate = true;
        //$scope.mapserverUpdated = false;
        $scope.staticPageSelected = v;
      };

      $scope.saveStaticPage = function () {
        var sp = Object.assign({}, $scope.staticPageSelected);
        delete sp.status;
        delete sp.sections;
        delete sp.section;
        delete sp.$$hashKey;

        $http
          .post(
            "../api/pages/" +  ($scope.isUpdate ? $scope.staticPageSelected.language + "/" + $scope.staticPageSelected.pageId : "") + "?" +
            gnUrlUtils.toKeyValue(sp)
          )
          .success(function (data) {
            $http
              .post(
                "../api/pages/" + $scope.staticPageSelected.language + "/" + $scope.staticPageSelected.pageId + "/" + $scope.staticPageSelected.section
              )
              .success(function (data) {
                $http
                  .put(
                    "../api/pages/" + $scope.staticPageSelected.language + "/" + $scope.staticPageSelected.pageId + "/" + $scope.staticPageSelected.status
                  )
                  .success(function (data) {
                    loadStaticPages();
                    $rootScope.$broadcast("StatusUpdated", {
                      msg: $translate.instant("staticPageUpdated"),
                      timeout: 2,
                      type: "success"
                    });
                  })
                  .error(function (data) {
                    $rootScope.$broadcast("StatusUpdated", {
                      title: $translate.instant("staticPageUpdateError"),
                      error: data,
                      timeout: 0,
                      type: "danger"
                    });
                  });
              })
              .error(function (data) {
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("staticPageUpdateError"),
                  error: data,
                  timeout: 0,
                  type: "danger"
                });
              });
          })
          .error(function (data) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("staticPageUpdateError"),
              error: data,
              timeout: 0,
              type: "danger"
            });
          });
      }

      $scope.deleteStaticPageConfig = function () {
        $("#gn-confirm-remove-static-page").modal("show");
      };

      $scope.confirmDeleteStaticPageConfig = function () {
        $http
          .delete("../api/pages/" + $scope.staticPageSelected.language + "/" + $scope.staticPageSelected.linkText + "?format=" + $scope.staticPageSelected.format)
          .success(function (data) {
            loadStaticPages();
          })
          .error(function (data) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("staticPageDeleteError"),
              error: data,
              timeout: 0,
              type: "danger"
            });
          });
      };

      loadStaticPages();
    }
  ]);
})();
