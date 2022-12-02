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
  goog.provide("gn_usersearches_directive");

  var module = angular.module("gn_usersearches_directive", []);

  module.directive("gnPortalSwitcher", [
    "$http",
    "gnGlobalSettings",
    function ($http, gnGlobalSettings) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: "../../catalog/components/usersearches/partials/portalswitcher.html",
        link: function postLink(scope, element, attrs) {
          scope.showPortalSwitcher =
            gnGlobalSettings.gnCfg.mods.header.showPortalSwitcher;

          function getPortals() {
            var url = "../api/sources/subportal";
            $http.get(url).then(function (response) {
              scope.portals = response.data.filter(function (p) {
                return p.uuid != scope.nodeId;
              });
            });
          }
          if (scope.showPortalSwitcher) {
            getPortals();
          }
        }
      };
    }
  ]);

  /**
   * Directive to display featured user searches in the home page.
   *
   */
  module.directive("gnUserSearchesList", [
    "gnUserSearchesService",
    "gnConfigService",
    "gnConfig",
    "gnLangs",
    "$http",
    "$translate",
    "$location",
    "gnGlobalSettings",
    function (
      gnUserSearchesService,
      gnConfigService,
      gnConfig,
      gnLangs,
      $http,
      $translate,
      $location,
      gnGlobalSettings
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: function (elem, attrs) {
          return (
            "../../catalog/components/usersearches/partials/featuredusersearches" +
            (attrs["mode"] || "cards") +
            ".html"
          );
        },
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          scope.type = attrs["type"] || "h";
          scope.withPortal =
            scope.isDefaultNode &&
            gnGlobalSettings.gnCfg.mods.search.usersearches.includePortals;

          scope.sortByLabel = function (i) {
            return i.names[scope.lang];
          };
          gnUserSearchesService
            .loadFeaturedUserSearches(scope.type, scope.withPortal)
            .then(
              function (featuredSearchesCollection) {
                scope.featuredSearches = featuredSearchesCollection.data;
              },
              function () {
                // TODO: Log error
              }
            );

          scope.search = function (url) {
            $location.path("/search").search(url);
          };
        }
      };
    }
  ]);

  /**
   * Directive to display the user searches panel in the search page.
   *
   */
  module.directive("gnUserSearchesPanel", [
    "gnUserSearchesService",
    "gnConfigService",
    "gnConfig",
    "gnUtilityService",
    "gnAlertService",
    "gnLangs",
    "gnGlobalSettings",
    "$http",
    "$translate",
    "$location",
    "$filter",
    "$route",
    function (
      gnUserSearchesService,
      gnConfigService,
      gnConfig,
      gnUtilityService,
      gnAlertService,
      gnLangs,
      gnGlobalSettings,
      $http,
      $translate,
      $location,
      $filter,
      $route
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          user: "=gnUserSearchesPanel"
        },
        templateUrl:
          "../../catalog/components/usersearches/partials/usersearchespanel.html",
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          // Configure the base url to launch the user search selected (search or board page)
          if (
            $route &&
            $route.current &&
            $route.current.$$route.originalPath === "/board"
          ) {
            scope.routeSearch = "board";
          } else {
            scope.routeSearch = "search";
          }
          scope.isUserSearchesEnabled =
            gnGlobalSettings.gnCfg.mods.search.usersearches.enabled;

          scope.userSearches = null;
          scope.currentSearch = null;

          scope.userSearchesSearch = "";

          scope.$watch("user", function (n, o) {
            if (n !== o || scope.userSearches === null) {
              scope.userSearches = null;

              scope.loadUserSearches();
            }
          });

          scope.loadUserSearches = function () {
            gnUserSearchesService.loadUserSearches().then(
              function (featuredSearchesCollection) {
                scope.userSearches = featuredSearchesCollection.data;

                angular.forEach(scope.userSearches, function (search) {
                  search.label =
                    search.names[scope.lang] ||
                    search.names["eng"] ||
                    $filter("translate")("userSearchNameMissing");
                });
              },
              function () {
                // TODO: Log error
              }
            );
          };

          scope.isUserSearchPanelEnabled = function () {
            return (
              scope.isUserSearchesEnabled && scope.user && scope.user.id !== undefined
            );
          };

          scope.canManageUserSearches = function () {
            // Check user is available, initialized and is administrator
            return scope.user && !_.isEmpty(scope.user) && scope.user.isAdministrator();
          };

          scope.search = function (url) {
            $location.path("/search").search(url);
          };

          scope.editUserSearch = function (search) {
            scope.openSaveUserSearchPanel(search);
          };

          scope.canEditUserSearch = function (search) {
            return (
              scope.user.id &&
              (search.creatorId == scope.user.id || scope.user.isAdministrator())
            );
          };

          scope.removeUserSearch = function (search) {
            return gnUserSearchesService.removeUserSearch(search).then(
              function () {
                gnAlertService.addAlert({
                  msg: $translate.instant("userSearchRemoved"),
                  type: "success"
                });

                scope.loadUserSearches();
              },
              function (reason) {
                gnAlertService.addAlert({
                  msg: reason.data,
                  type: "danger"
                });
              }
            );
          };

          scope.openSaveUserSearchPanel = function (search) {
            scope.currentSearch = angular.copy(search);
            if (scope.currentSearch) {
              delete scope.currentSearch.label;
            }

            gnUtilityService.openModal(
              {
                title: "userSearch",
                content:
                  '<div gn-save-user-search="currentSearch" data-user="user"></div>',
                className: "gn-savesearch-popup",
                onCloseCallback: function () {
                  scope.loadUserSearches();
                }
              },
              scope,
              "UserSearchUpdated"
            );
          };

          scope.openAdminUserSearchPanel = function () {
            gnUtilityService.openModal(
              {
                title: "manageUserSearchesTitle",
                content: '<div gn-user-search-manager="user"></div>',
                className: "gn-searchmanager-popup",
                onCloseCallback: function () {
                  scope.loadUserSearches();
                }
              },
              scope,
              "UserSearchesManagementClose"
            );
          };
        }
      };
    }
  ]);

  /**
   * Directive for the user search create/update panel.
   *
   */
  module.directive("gnSaveUserSearch", [
    "gnUserSearchesService",
    "gnConfigService",
    "gnConfig",
    "gnLangs",
    "gnGlobalSettings",
    "$http",
    "$translate",
    "$location",
    "$httpParamSerializer",
    "$timeout",
    function (
      gnUserSearchesService,
      gnConfigService,
      gnConfig,
      gnLangs,
      gnGlobalSettings,
      $http,
      $translate,
      $location,
      $httpParamSerializer,
      $timeout
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          userSearch: "=gnSaveUserSearch",
          user: "="
        },
        templateUrl: "../../catalog/components/usersearches/partials/saveusersearch.html",
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          scope.updateSearchUrl = false;

          scope.availableLangs = gnGlobalSettings.gnCfg.mods.header.languages;
          scope.langList = angular.copy(scope.availableLangs);
          angular.forEach(scope.langList, function (lang2, lang3) {
            scope.langList[lang3] = "#" + lang2;
          });
          scope.currentLangShown = scope.lang;

          scope.userSearchGroups = { choices: [], groups: [] };

          // Retrieve groups for current user when creating a new
          // user search or the user creator groups
          var userIdForGroups = scope.user.id;

          if (scope.userSearch && scope.user.isAdministratorOrMore()) {
            userIdForGroups = scope.userSearch.creatorId;
          }

          function loadUserGroup() {
            $http.get("../api/users/" + userIdForGroups + "/groups").then(
              function (response) {
                var data = response.data;
                var choices = [];

                // Remove internal groups
                for (var i = 0; i < data.length; i++) {
                  if (data[i].group.id > 1) {
                    var g = data[i].group;
                    g.langlabel = g.label[scope.lang] || g.name;
                    choices.push(g);
                  }
                }
                scope.userSearchGroups.choices = choices;

                var searchGroup = [];
                scope.userSearchGroupsTextList = "";

                if (scope.userSearch && scope.userSearch.groups.length > 0) {
                  for (var i = 0; i < scope.userSearch.groups.length; i++) {
                    var groupId = scope.userSearch.groups[i];
                    searchGroup = searchGroup.concat(
                      scope.userSearchGroups.choices.filter(function (group) {
                        return group.id === groupId;
                      })
                    );
                  }
                  scope.userSearchGroupsTextList = scope.userSearch.groups.join(",");
                }
                scope.userSearchGroups.groups = searchGroup;
              },
              function (response) {
                // TODO
              }
            );
          }

          //loadUserGroup();
          $timeout(loadUserGroup, 200);

          scope.$watchCollection("userSearchGroups.groups", function (n, o) {
            if (n !== o) {
              scope.userSearchGroupsTextList = "";

              for (var j = 0; j < n.length; j++) {
                var g = n[j];
                scope.userSearchGroupsTextList += g.id;

                if (j != n.length - 1) {
                  scope.userSearchGroupsTextList += ",";
                }
              }
            }
          });

          var retrieveSearchParameters = function () {
            var searchParams = angular.copy($location.search());

            delete searchParams.from;
            delete searchParams.to;
            delete searchParams.fast;
            delete searchParams._content_type;

            return $httpParamSerializer(searchParams);
          };

          if (!scope.userSearch) {
            scope.userSearch = {
              url: retrieveSearchParameters(),
              id: 0,
              creatorId: scope.user.id,
              featuredType: "",
              names: {},
              groups: []
            };

            scope.editMode = false;
          } else {
            scope.editMode = true;
          }

          /**
           * Checks if the user can set the feature type info in a search:
           *  - The user is has Administrator profile.
           *  - The search has been created by the same user.
           *
           * @returns {*}
           */
          scope.canSetFeatureTypeInSearch = function () {
            return (
              scope.user &&
              scope.userSearch &&
              scope.user.isAdministratorOrMore() &&
              scope.user.id == scope.userSearch.creatorId
            );
          };

          scope.updateUrl = function () {
            scope.userSearch.url = retrieveSearchParameters();
          };

          scope.toggleFeaturedType = function () {
            if (scope.userSearch.featuredType === "") {
              // Default value
              scope.userSearch.featuredType = "h";
            } else {
              scope.userSearch.featuredType = "";
            }
          };

          scope.isFeaturedSearch = function () {
            return scope.userSearch && scope.userSearch.featuredType !== "";
          };

          scope.saveUserSearch = function () {
            var userSearch = angular.copy(scope.userSearch);
            delete userSearch.title;

            if (scope.isFeaturedSearch()) {
              // Featured searches are public, can't be associated to groups
              userSearch.groups = [];
            } else {
              userSearch.groups = scope.userSearchGroupsTextList.split(",");
            }

            return gnUserSearchesService.saveUserSearch(userSearch).then(
              function (response) {
                scope.$emit("UserSearchUpdated", true);
                scope.$emit("StatusUpdated", {
                  msg: $translate.instant("userSearchUpdated"),
                  timeout: 0,
                  type: "success"
                });
              },
              function (response) {
                scope.$emit("StatusUpdated", {
                  title: $translate.instant("userSearchUpdatedError"),
                  error: response.data,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
          };
        }
      };
    }
  ]);

  /**
   * Directive for the user searches manager.
   *
   */
  module.directive("gnUserSearchManager", [
    "gnUserSearchesService",
    "gnConfigService",
    "gnConfig",
    "gnUtilityService",
    "gnAlertService",
    "gnLangs",
    "$http",
    "$translate",
    "$filter",
    "$compile",
    function (
      gnUserSearchesService,
      gnConfigService,
      gnConfig,
      gnUtilityService,
      gnAlertService,
      gnLangs,
      $http,
      $translate,
      $filter,
      $compile
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          user: "=gnUserSearchManager"
        },
        templateUrl:
          "../../catalog/components/usersearches/partials/usersearchesmanager.html",
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          scope.userSearches = [];

          scope.tableEl = element;

          $http.get("../api/groups").then(
            function (response) {
              scope.groups = response.data;
              scope.bsTableControl = {
                options: {
                  locale: "en",
                  url: "../api/usersearches/allpaginated",
                  responseHandler: function (result) {
                    scope.userSearches = angular.copy(result.rows);

                    // Calculate the label to display
                    angular.forEach(result.rows, function (search) {
                      search.label =
                        search.names[scope.lang] ||
                        search.names["eng"] ||
                        $filter("translate")("userSearchNameMissing");
                    });

                    return result;
                  },
                  rowStyle: function (row, index) {
                    return { classes: "none" };
                  },
                  onPostBody: function (data) {
                    $compile(scope.tableEl.contents())(scope);
                    return true;
                  },
                  cache: false,
                  striped: true,
                  sidePagination: "server",
                  pagination: true,
                  pageSize: scope.pageSize,
                  pageList: [5, 10, 50, 100, 200],
                  search: true,
                  minimumCountColumns: 2,
                  clickToSelect: false,
                  columns: [
                    {
                      field: "label",
                      title: $filter("translate")("userSearchTblSearchName"),
                      valign: "bottom",
                      sortable: false,
                      formatter: function (value, row) {
                        return (
                          '<span class="fa ' +
                          (row.featuredType !== "" ? "fa-star" : "") +
                          ' fa-fw" title="' +
                          $filter("translate")("featuredsearch") +
                          '"></span>' +
                          row.label
                        );
                      }
                    },
                    {
                      field: "creator",
                      title: $filter("translate")("userSearchTblCreator"),
                      valign: "middle",
                      sortable: false
                    },
                    {
                      field: "creationDate",
                      title: $filter("translate")("userSearchTblCreationDate"),
                      align: "center",
                      valign: "middle",
                      sortable: false
                    },
                    {
                      field: "groups",
                      title: $filter("translate")("userSearchTblGroups"),
                      valign: "middle",
                      sortable: false,
                      formatter: function (value, row) {
                        var groupNames = [];

                        if (angular.isArray(value)) {
                          for (var i = 0; i < value.length; i++) {
                            var groupId = value[i];

                            var group = scope.groups.filter(function (group) {
                              return group.id === groupId;
                            });

                            if (group) {
                              groupNames.push(group[0].label[scope.lang]);
                            }
                          }
                        }

                        return groupNames.join(",");
                      }
                    },
                    {
                      title: "",
                      width: 75,
                      formatter: function (value, row, index) {
                        return (
                          '<div class="btn-group pull-right" role="group">' +
                          '  <a class="btn btn-default btn-xs" data-ng-click="editUserSearch(' +
                          row.id +
                          ')">' +
                          '    <span class="fa fa-pencil"></span>' +
                          "  </a>" +
                          '  <a class="btn btn-default btn-xs" data-gn-confirm-click="{{\'deleteUserSearchConfirm\' | translate }}"  ' +
                          "title=\"{{'delete' | translate}}\" " +
                          'data-gn-click-and-spin="removeUserSearch(' +
                          row.id +
                          ' )">' +
                          '    <span class="fa fa-times text-danger"></span>' +
                          "  </a>" +
                          "</div>"
                        );
                      }
                    }
                  ]
                }
              };
            },
            function (response) {
              // TODO
            }
          );

          var findUserSeachById = function (userSearches, searchId) {
            var search = _.find(userSearches, function (search) {
              return search.id == searchId;
            });

            return search;
          };

          var refreshTable = function () {
            $("#tbl-user-searches-manager").bootstrapTable("refresh");
          };

          scope.removeUserSearch = function (searchId) {
            var search = findUserSeachById(scope.userSearches, searchId);

            return gnUserSearchesService.removeUserSearch(search).then(
              function () {
                gnAlertService.addAlert({
                  msg: $translate.instant("userSearchRemoved"),
                  type: "success"
                });

                refreshTable();
              },
              function (reason) {
                gnAlertService.addAlert({
                  msg: reason.data,
                  type: "danger"
                });
              }
            );
          };

          scope.editUserSearch = function (searchId) {
            scope.currentSearch = findUserSeachById(scope.userSearches, searchId);

            gnUtilityService.openModal(
              {
                title: "savesearch",
                content:
                  '<div gn-save-user-search="currentSearch" data-user="user"></div>',
                className: "gn-savesearch-popup",
                onCloseCallback: function () {
                  refreshTable();
                }
              },
              scope,
              "UserSearchUpdated"
            );
          };
        }
      };
    }
  ]);
})();
