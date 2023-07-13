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
  goog.provide("gn_static_pages_directive");

  var module = angular.module("gn_static_pages_directive", ["ngSanitize"]);

  module.directive("gnStaticPagesViewer", [
    "$http",
    "$location",
    "$sce",
    function ($http, $location, $sce) {
      return {
        restrict: "AEC",
        replace: true,
        scope: {
          language: "@language"
        },
        templateUrl: "../../catalog/components/pages/partials/content.html",
        link: function ($scope) {
          $scope.loadPageContent = function () {
            var page = $location.search().page;

            $http({
              method: "GET",
              url: "../api/pages/" + $scope.language + "/" + page + "/content",
              transformResponse: angular.identity
            }).then(
              function (response) {
                $sce.trustAsJs(response.data);
                // $sce.trustAsHtml(response.data);
                $scope.content = $sce.trustAsHtml(response.data);
              },
              function (response) {
                $scope.content = "Page not available";
              }
            );
          };

          function reloadPageContent() {
            $scope.loadPageContent();
          }

          $scope.$on("$locationChangeSuccess", reloadPageContent);

          $scope.loadPageContent();
        }
      };
    }
  ]);

  module.directive("gnStaticPagesListViewer", [
    "gnGlobalSettings",
    "gnStaticPagesService",
    function (gnGlobalSettings, gnStaticPagesService) {
      return {
        restrict: "AEC",
        replace: false,
        scope: {
          language: "@language",
          section: "@section"
        },
        templateUrl: function (elem, attr) {
          return "../../catalog/components/pages/partials/top.html";
        },
        link: function ($scope) {
          $scope.pagesMenu = [];

          var configKey = $scope.section === "footer" ? "footer" : "header";
          $scope.pagesConfig =
            gnGlobalSettings.gnCfg.mods[configKey][$scope.section + "CustomMenu"];

          if ($scope.pagesConfig && $scope.pagesConfig.length === 0) {
            gnStaticPagesService
              .loadPages($scope.language, $scope.section)
              .then(function (response) {
                $scope.pagesConfig = response.data.map(function (p) {
                  return p.pageId;
                });
              });
          }
        }
      };
    }
  ]);

  module.directive("gnStaticPageMenu", [
    "gnStaticPagesService",
    "gnGlobalSettings",
    function (gnStaticPagesService, gnGlobalSettings) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          pageId: "=gnStaticPageMenu",
          language: "@language",
          section: "@section"
        },
        templateUrl: function (elem, attr) {
          return "../../catalog/components/pages/partials/menu-page.html";
        },
        link: function ($scope) {
          $scope.pagesMenu = [];
          $scope.gnCfg = gnGlobalSettings.gnCfg;
          $scope.pagesConfig = angular.isArray($scope.pageId)
            ? $scope.pageId
            : [$scope.pageId];

          if ($scope.pagesConfig.length > 0) {
            gnStaticPagesService.loadPages($scope.language, $scope.section).then(
              function (response) {
                $scope.pagesMenu = [];
                $scope.pages = {};
                response.data.forEach(function (page) {
                  $scope.pages[page.pageId] = page;
                });

                gnStaticPagesService.buildMenu(
                  $scope.pagesMenu,
                  $scope.pages,
                  $scope.pagesConfig
                );
                if ($scope.pagesMenu.length === 1) {
                  $scope.page = $scope.pagesMenu[0];
                  $scope.isSubmenu = $scope.page.type === "submenu";
                  $scope.isExternalLink =
                    $scope.page.format == "LINK" || $scope.page.format == "HTMLPAGE";
                }
              },
              function (response) {
                $scope.pagesList = null;
              }
            );
          }
        }
      };
    }
  ]);
})();
