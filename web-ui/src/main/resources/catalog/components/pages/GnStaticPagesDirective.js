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

          if ($scope.section === "header" || $scope.section === "footer") {
            $scope.pagesConfig =
              gnGlobalSettings.gnCfg.mods[$scope.section][$scope.section + "CustomMenu"];
            if ($scope.pagesConfig && $scope.pagesConfig.length === 0) {
              gnStaticPagesService
                .loadPages($scope.language, $scope.section)
                .then(function (response) {
                  $scope.pagesConfig = response.data.map(function (p) {
                    return p.pageId;
                  });
                });
            }
          } else {
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

  /**
   * Render static page menu entries for a configured section.
   *
   * Accepts a page id, an array of page ids, or nested submenu config through
   * `gn-static-page-menu`.
   */
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
          section: "@section",
          renderAsButton: "@?",
          context: "=?"
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

          // Set button style based on explicit parameter (defaults to false)
          $scope.renderAsButton = $scope.renderAsButton === "true";

          /**
           * Apply runtime visibility rules for a page.
           *
           * In `record_view_menu`, visibility depends on workflow state and
           * metadata approval status from the current record context.
           */
          function shouldShowPage(page) {
            if (!page) return false;
            if (
              $scope.section === "record_view_menu" &&
              $scope.context &&
              $scope.context.currentRecord
            ) {
              var currentRecord = $scope.context.currentRecord;
              if (!currentRecord.isWorkflowEnabled())
                return page.showWhenWorkflowDisabled === true;
              if (currentRecord.mdStatus === "2") return page.showOnApproved === true;
              return page.showOnNonApproved === true;
            }
            return true;
          }

          /**
           * Recursively build a display-ready menu from `pagesConfig`.
           *
           * `pagesConfig` may contain page ids (strings) and submenu descriptors.
           * Unknown page ids are ignored and logged to help diagnose UI config issues.
           */
          function buildMenu(pagesMenu, staticPages, pagesConfig) {
            pagesConfig.forEach(function (menu) {
              if (typeof menu === "string") {
                if (staticPages[menu]) {
                  if (shouldShowPage(staticPages[menu])) {
                    pagesMenu.push(staticPages[menu]);
                  }
                } else {
                  console.warn(
                    menu +
                      " not found in pages configuration." +
                      " Check your UI configuration."
                  );
                }
              } else if (angular.isObject(menu)) {
                var key = Object.keys(menu)[0];
                var value = menu[key];

                var submenu = {
                  label: key,
                  icon: undefined,
                  type: "submenu",
                  pages: []
                };

                var menuItems;

                // If the submenu is using the legacy array format
                if (angular.isArray(value)) {
                  menuItems = value;
                  // If the submenu is using the new object format with an items array and optional icon
                } else if (angular.isObject(value)) {
                  if (angular.isArray(value.items)) {
                    menuItems = value.items;
                  }
                  if (angular.isDefined(value.icon)) {
                    submenu.icon = value.icon;
                  }
                } else {
                  console.warn(
                    "Invalid menu configuration for " +
                      key +
                      ". Expected an array of page identifiers or an object with an items array."
                  );
                  return;
                }

                buildMenu(submenu.pages, staticPages, menuItems);

                if (submenu.pages.length > 0) {
                  pagesMenu.push(submenu);
                }
              }
            });
            return pagesMenu;
          }

          /**
           * Recompute rendered menu items from loaded pages and current config.
           *
           * When a single item is produced, expose it as `$scope.page` for
           * simplified template handling; otherwise keep list rendering mode.
           */
          function rebuildMenu() {
            if (!$scope.pages) {
              return;
            }

            $scope.pagesMenu = [];
            buildMenu($scope.pagesMenu, $scope.pages, $scope.pagesConfig);

            if ($scope.pagesMenu.length === 1) {
              $scope.page = $scope.pagesMenu[0];
              $scope.isSubmenu = $scope.page.type === "submenu";
              $scope.isExternalLink =
                $scope.page.format === "LINK" ||
                $scope.page.format === "EMAILLINK" ||
                $scope.page.format === "HTMLPAGE";

              if (
                $scope.page.format === "EMAILLINK" &&
                $scope.page.link &&
                !$scope.page.link.startsWith("mailto:")
              ) {
                $scope.page.link = "mailto:" + $scope.page.link;
              }
            } else {
              $scope.page = null;
            }
          }

          if ($scope.pagesConfig.length > 0) {
            gnStaticPagesService.loadPages($scope.language, $scope.section).then(
              function (response) {
                $scope.pagesMenu = [];
                $scope.pages = {};
                response.data.forEach(function (page) {
                  $scope.pages[page.pageId] = page;
                });

                rebuildMenu();
              },
              function (response) {
                $scope.pagesList = null;
              }
            );

            // Rebuild visibility-dependent menu items when context or any nested value changes.
            $scope.$watch(
              "context",
              function () {
                rebuildMenu();
              },
              true
            );
          }
        }
      };
    }
  ]);
})();
