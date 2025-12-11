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
  goog.provide("gn_admintools_controller");

  goog.require("gn_search");
  goog.require("gn_search_form_controller");
  goog.require("gn_utility_service");

  var module = angular.module("gn_admintools_controller", [
    "gn_search",
    "gn_search_form_controller",
    "gn_utility_service"
  ]);

  module.controller("GnAdminToolsSearchController", [
    "$scope",
    "gnSearchSettings",
    function ($scope, gnSearchSettings) {
      var defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: "b101",
        filters: gnSearchSettings.filters,
        params: {
          sortBy: "changeDate",
          sortOrder: "desc",
          _isTemplate: "y or n",
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, defaultSearchObj);

      $scope.setTemplate = function (params) {
        var values = [];
        if ($("#batchSearchTemplateY")[0].checked) values.push("y");
        if ($("#batchSearchTemplateN")[0].checked) values.push("n");
        if ($("#batchSearchTemplateS")[0].checked) values.push("s");
        $scope.searchObj.params.isTemplate = values.join(" or ");
      };
    }
  ]);

  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller("GnAdminToolsController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "$compile",
    "$q",
    "$timeout",
    "$routeParams",
    "$location",
    "gnSearchManagerService",
    "gnConfigService",
    "gnUtilityService",
    "gnSearchSettings",
    "gnGlobalSettings",

    function (
      $scope,
      $http,
      $rootScope,
      $translate,
      $compile,
      $q,
      $timeout,
      $routeParams,
      $location,
      gnSearchManagerService,
      gnConfigService,
      gnUtilityService,
      gnSearchSettings,
      gnGlobalSettings
    ) {
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

      $scope.pageMenu = {
        folder: "tools/",
        defaultTab: "index",
        tabs: [
          {
            type: "index",
            label: "catalogueAdminTools",
            icon: "fa-search",
            href: "#/tools/index"
          },
          {
            type: "transferownership",
            label: "transfertPrivs",
            icon: "fa-user",
            href: "#/tools/transferownership"
          }
        ]
      };

      function loadEditors() {
        $http.get("../api/users/owners").then(function (response) {
          $scope.editors = response.data;
        });
        $http.get("../api/users/groups?groupTypes=Workspace").then(function (response) {
          var uniqueUserGroups = {};
          angular.forEach(response.data, function (g) {
            var key = g.groupId + "-" + g.userId;
            if (!uniqueUserGroups[key]) {
              uniqueUserGroups[key] = g;
              uniqueUserGroups[key].groupNameTranslated =
                g.groupName === "allAdmins"
                  ? $translate.instant(g.groupName)
                  : $translate.instant("group-" + g.groupId);
            }
          });

          // Sort by group name and user name
          var sortedKeys = Object.keys(uniqueUserGroups).sort(function (a, b) {
            var ka =
              uniqueUserGroups[a].groupNameTranslated +
              "|" +
              uniqueUserGroups[a].userName;
            var kb =
              uniqueUserGroups[b].groupNameTranslated +
              "|" +
              uniqueUserGroups[b].userName;

            return ka.localeCompare(kb);
          });

          $scope.userGroups = {};
          angular.forEach(sortedKeys, function (g) {
            $scope.userGroups[g] = uniqueUserGroups[g];
          });
        });
      }
      $scope.selectUser = function (id) {
        $scope.editorSelectedId = id;
        $http.get("../api/users/" + id + "/groups").then(function (response) {
          var uniqueGroup = {};
          angular.forEach(response.data, function (g) {
            // Only include workspace groups
            if (g.group.type === "Workspace" && !uniqueGroup[g.group.id]) {
              uniqueGroup[g.group.id] = g.group;
            }
          });

          // Sort the groups by group name translation
          $scope.editorGroups = Object.values(uniqueGroup).sort(function (a, b) {
            return a.label[$scope.lang].localeCompare(b.label[$scope.lang]);
          });
        });
      };
      $scope.transfertList = {};

      $scope.tranferOwnership = function (sourceGroup) {
        var params = $scope.transfertList[sourceGroup];

        params.running = true;
        return $http
          .put("../api/users/owners", {
            sourceUser: parseInt($scope.editorSelectedId),
            sourceGroup: parseInt(sourceGroup),
            targetUser: params.targetGroup.userId,
            targetGroup: params.targetGroup.groupId
          })
          .then(
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("transfertPrivilegesFinished", {
                  privileges: response.data.privileges,
                  metadata: response.data.metadata
                }),
                timeout: 2,
                type: "success"
              });
              params.running = false;
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("transfertPrivilegesError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
              params.running = false;
            }
          );
      };
      $scope.isRunning = function (sourceGroup) {
        return $scope.transfertList[sourceGroup].running;
      };

      loadEditors();

      /**
       * Inform if indexing is ongoing or not
       */
      $scope.isIndexing = false;

      /**
       * Number of records in the index
       */
      $scope.numberOfIndexedRecords = null;

      /**
       * Check index every ...
       */
      var indexCheckInterval = 5000;

      /**
       * Get number of record in the index and
       * then check if indexing is ongoing or not every
       * indexCheckInterval. Stop when not indexing.
       *
       * TODO: Could we kill the check when switching to somewhere else?
       */
      function checkIsIndexing() {
        // Check if indexing
        return $http.get("../api/site/indexing").then(function (response) {
          $scope.isIndexing = response.data;
          if ($scope.isIndexing) {
            $timeout(checkIsIndexing, indexCheckInterval);
          }
          // Get the number of records (template, records, subtemplates)
          $http
            .post("../api/search/records/_search", { size: 0 })
            .then(function (response) {
              $scope.numberOfIndexedRecords = response.data.hits.total;
            });
        });
      }

      checkIsIndexing();

      $scope.rebuildIndex = function (dropFirst, index) {
        var url =
          "../api/site/index?reset=" + dropFirst + (index ? "&indices=" + index : "");
        return $http.put(url).then(
          function (response) {
            checkIsIndexing();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("rebuildIndexError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.clearJsCache = function () {
        return $http.get("../../static/wroAPI/reloadModel").then(function (response) {
          $http.get("../../static/wroAPI/reloadCache").then(function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("jsCacheCleared"),
              timeout: 2,
              type: "success"
            });
          });
        });
      };

      $scope.clearFormatterCache = function () {
        return $http.delete("../api/formatters/cache").then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("formatterCacheCleared"),
              timeout: 2,
              type: "success"
            });
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("formatCacheClearFailure"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.translationPackClearCache = function () {
        return $http.delete("../api/i18n/cache").then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("translationPackCacheCleared"),
              timeout: 2,
              type: "success"
            });
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("translationPackCacheClearFailure"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      gnConfigService.loadPromise.then(function (settings) {
        $scope.isBackupArchiveEnabled = settings["metadata.backuparchive.enable"];
      });

      $scope.triggerBackupArchive = function () {
        return $http({ method: "PUT", url: "../api/records/backups" }).then(function (
          data
        ) {
          $rootScope.$broadcast("StatusUpdated", {
            title: $translate.instant("generatingArchiveBackup"),
            error: data,
            timeout: 2,
            type: "success"
          });
        });
      };
    }
  ]);
})();
