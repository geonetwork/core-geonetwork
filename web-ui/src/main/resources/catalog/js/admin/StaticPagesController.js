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

  var module = angular.module("gn_static_pages_controller", ["blueimp.fileupload"]);

  module.controller("GnStaticPagesController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "$log",
    "gnGlobalSettings",
    "gnUtilityService",
    function (
      $scope,
      $http,
      $rootScope,
      $translate,
      $log,
      gnGlobalSettings,
      gnUtilityService
    ) {
      $scope.dbLanguages = [];
      $scope.staticPages = [];
      $scope.formats = [];
      $scope.sections = [];
      $scope.staticPageSelected = null;
      $scope.queue = [];
      $scope.uploadScope = angular.element("#gn-static-page-edit").scope();
      $scope.groups = [];

      $scope.unsupportedFile = false;
      $scope.$watchCollection("queue", function (n, o) {
        if (n != o && n.length == 1) {
          if (n[0].name.match(/.html$/i) !== null) {
            $scope.unsupportedFile = false;
          } else {
            $scope.unsupportedFile = true;
          }
          return;
        }
        $scope.unsupportedFile = false;
      });

      function loadFormats() {
        $http.get("../api/pages/config/formats").then(function (r) {
          $scope.formats = r.data;
        });
        $http.get("../api/pages/config/sections").then(function (r) {
          $scope.sections = r.data;
        });
      }

      function loadGroups() {
        $http.get("../api/groups").then(function (r) {
          $scope.groups = gnUtilityService.sortByTranslation(r.data, $scope.lang, "name");
        });
      }

      function loadStaticPages() {
        $scope.staticPageSelected = null;
        $http.get("../api/pages").then(function (r) {
          $scope.staticPages = r.data;
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

      var successHandler = function () {
        $scope.queue = [];
        loadStaticPages();
        $rootScope.$broadcast("StatusUpdated", {
          msg: $translate.instant("staticPageUpdated"),
          timeout: 2,
          type: "success"
        });
      };

      var failureHandler = function (error) {
        $rootScope.$broadcast("StatusUpdated", {
          title: $translate.instant("staticPageUpdateError"),
          error: error,
          timeout: 0,
          type: "danger"
        });
      };

      /** Upload management */
      var uploadStaticPageFileDone = function (e, data) {
        $scope.staticPageSelected.data = data.files[0].name;
        $scope.clear(data.files[0]);
        successHandler();
      };
      var uploadStaticPageFileError = function (event, data) {
        var req = data.response().jqXHR;
        var contentType = req.getResponseHeader("Content-Type");
        var errorText = req.responseText;
        var parsedError = null;
        if ("application/json" === contentType) {
          try {
            parsedError = JSON.parse(req.responseText);
          } catch (e) {
            $log.warn("Error converting response in JSON object: " + req.responseText);
          }
        }
        failureHandler(parsedError || errorText);
      };

      // upload directive options
      $scope.mdStaticPageFileUploadOptions = {
        autoUpload: false,
        maxNumberOfFiles: 1,
        done: uploadStaticPageFileDone,
        fail: uploadStaticPageFileError,
        headers: { "X-XSRF-TOKEN": $rootScope.csrf, "Accept-Language": $scope.lang }
      };

      $scope.$on("fileuploadchange", function (e, data) {
        // limit fileupload to only one file.
        angular.forEach($scope.queue, function (item) {
          $scope.clear(item);
        });
      });

      $scope.addStaticPage = function () {
        $scope.isUpdate = false;
        $scope.isGroupEnabled = false;
        $scope.staticPageSelected = {
          language: "",
          pageId: "",
          format: "LINK",
          link: "",
          data: "",
          content: "",
          status: "HIDDEN",
          groups: "",
          label: "",
          sections: []
        };

        $scope.pageApiLink = "";

        $scope.action = "../api/pages";
      };

      $scope.selectStaticPage = function (v) {
        $scope.isUpdate = true;
        $scope.staticPageSelected = v;
        $scope.isGroupEnabled = $scope.staticPageSelected.status == "GROUPS";

        var link =
          "api/pages/" +
          $scope.staticPageSelected.language +
          "/" +
          $scope.staticPageSelected.pageId;
        $scope.action = "../" + link;

        $scope.content = "";
        $scope.pageApiLink = gnGlobalSettings.nodeUrl + link + "/content";
        if ($scope.staticPageSelected.format !== "LINK") {
          $http
            .get($scope.action + "/content", { headers: { Accept: "text/html" } })
            .then(function (r) {
              $scope.staticPageSelected.content = r.data;
            });
        }
      };

      $scope.deleteContent = function () {
        $scope.staticPageSelected.link = "";
        $scope.staticPageSelected.content = "";
      };

      $scope.saveStaticPage = function () {
        var sp = Object.assign({}, $scope.staticPageSelected);
        delete sp.$$hashKey;

        if ($scope.isUpdate) {
          delete sp.language;
          delete sp.pageId;
        }

        var isFileUpload = $scope.queue.length === 1,
          action =
            "../api/pages" +
            ($scope.isUpdate
              ? "/" +
                $scope.staticPageSelected.language +
                "/" +
                $scope.staticPageSelected.pageId
              : "");

        if (isFileUpload) {
          $scope.enctype = "multipart/form-data";
          $scope.action = action;

          $scope.uploadScope.submit();
        } else {
          delete sp.data;

          // Reset empty string to null to avoid parsing error
          if (sp.groups == "") {
            sp.groups = null;
          }

          return $http
            .put(action, sp, {
              headers: {
                "Content-Type": "application/json"
              }
            })
            .then(successHandler, function (r) {
              failureHandler(r.data);
            });
        }
      };
      $scope.updateGroupSelection = function () {
        if ($scope.staticPageSelected.status === "GROUPS") {
          $scope.isGroupEnabled = true;
        } else {
          $scope.isGroupEnabled = false;
        }
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

      loadFormats();
      loadDbLanguages();
      loadStaticPages();
      loadGroups();
    }
  ]);
})();
