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
  goog.provide("gn_sources_controller");

  var module = angular.module("gn_sources_controller", []);

  module.controller("GnSourcesController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "gnESClient",
    "Metadata",
    function ($scope, $http, $rootScope, $translate, gnESClient, Metadata) {
      $scope.sources = [];
      $scope.uiConfigurations = [];
      $scope.source = null;
      $scope.filteredSources = null;
      $scope.filter = {
        types: { portal: true, subportal: true, externalportal: true, harvester: true }
      };

      $scope.serviceRecordSearchObj = {
        internal: true,
        any: "",
        defaultParams: {
          any: "",
          from: 1,
          to: 50,
          type: "service",
          isTemplate: "n",
          sortBy: "resourceTitleObject.default.sort",
          sortOrder: "asc"
        }
      };
      $scope.serviceRecordSearchObj.params = angular.extend(
        {},
        $scope.serviceRecordSearchObj.defaultParams
      );

      $scope.selectSource = function (source) {
        source.uiConfig = source.uiConfig && source.uiConfig.toString();
        source.groupOwner = source.groupOwner || null;
        $scope.source = source;
        $scope.isNew = false;

        $scope.gnSourceForm.$setPristine();
      };

      function filterSources() {
        $scope.filteredSources = [];
        $scope.sources.forEach(function (s) {
          if ($scope.filter.types[s.type] === true) {
            $scope.filteredSources.push(s);
          }
        });
      }

      $scope.$watch(
        "filter",
        function (n, o) {
          if (n !== o) {
            filterSources();
          }
        },
        true
      );

      function loadSources() {
        var url = "../api/sources";
        if ($scope.user.profile === "UserAdmin") {
          url += "?group=" + $scope.user.groupsWithUserAdmin.join("&group=");
        }
        $http.get(url).then(function (response) {
          $scope.sources = response.data;
          if ($scope.source && $scope.source.uuid !== null) {
            var selectedSource = _.find($scope.sources, { uuid: $scope.source.uuid });
            if (selectedSource) {
              $scope.source = selectedSource;
            }
          }
          filterSources();
          $scope.isNew = false;
        });
      }

      function loadUiConfigurations() {
        $scope.uiConfiguration = undefined;
        $scope.uiConfigurationId = "";
        $http.get("../api/ui").then(function (response) {
          var data = response.data;

          $scope.uiConfigurations = [{ id: "" }];
          for (var i = 0; i < data.length; i++) {
            $scope.uiConfigurations.push({
              id: data[i].id
            });
          }
        });
      }

      $scope.isNew = false;
      $scope.addSubPortal = function () {
        $scope.isNew = true;
        $scope.source = {
          type: "subportal",
          uuid: "",
          name: "",
          logo: "",
          uiConfig: "",
          filter: "",
          serviceRecord: null,
          groupOwner: null,
          listableInHeaderSelector: true
        };
        // TODO: init labels
      };

      $scope.updateSource = function () {
        var url = "../api/sources" + ($scope.isNew ? "" : "/" + $scope.source.uuid);
        $http.put(url, $scope.source).then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("sourceUpdated"),
              timeout: 2,
              type: "success"
            });

            loadSources();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("sourceUpdateError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.deleteSourceConfig = function () {
        $("#gn-confirm-remove-source").modal("show");
      };

      $scope.confirmDeleteSourceConfig = function () {
        $http.delete("../api/sources/" + $scope.source.uuid).then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("sourceRemoved"),
              timeout: 2,
              type: "success"
            });

            loadSources();
            $scope.source = null;
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("sourceRemovedError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      var uploadLogoDone = function (e, data) {
        $scope.source.logo = data.files[0].name;
        $scope.clear(data.files[0]);
        createOrModifyGroup();
      };
      var uploadLogoError = function (event, data) {
        var req = data.response().jqXHR;
        var contentType = req.getResponseHeader("Content-Type");
        var errorText = req.responseText;
        var errorCode = null;
        if ("application/json" === contentType) {
          var parsedError = JSON.parse(req.responseText);
        }
        $rootScope.$broadcast("StatusUpdated", {
          title: $translate.instant("groupUpdateError"),
          error: parsedError || errorText,
          timeout: 0,
          type: "danger"
        });
      };

      $scope.deleteSourceLogo = function () {
        $scope.source.logo = null;
        // $scope.updateSource();
      };

      // upload directive options
      $scope.logoUploadOptions = {
        autoUpload: true,
        url: "../api/logos?_csrf=" + $scope.csrf,
        dataType: "text",
        maxNumberOfFiles: 1,
        done: uploadLogoDone,
        fail: uploadLogoError
      };

      $scope.$on("fileuploadchange", function (e, data) {
        // limit fileupload to only one file.
        angular.forEach($scope.queue, function (item) {
          $scope.clear(item);
        });
      });

      loadSources();
      loadUiConfigurations();
    }
  ]);
})();
