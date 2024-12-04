/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
  goog.provide("gn_doiserver_controller");

  var module = angular.module("gn_doiserver_controller", []);

  /**
   * GnDoiServerController provides management interface
   * for DOI server configuration used for DOI publication.
   *
   */
  module.controller("GnDoiServerController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    function ($scope, $http, $rootScope, $translate) {
      $scope.doiServers = [];
      $scope.doiServerSelected = null;
      $scope.doiServerUpdated = false;
      $scope.doiServerSearch = "";
      $scope.isUpdate = null;
      $scope.selectedRecordGroups = [];
      $scope.recordGroups = [];

      // Load groups
      function loadGroups() {
        $http.get("../api/groups").then(
          function (response) {
            $scope.recordGroups = response.data;

            var getLabel = function (g) {
              return g.label[$scope.lang] || g.name;
            };

            angular.forEach($scope.recordGroups, function (u) {
              u.langlabel = getLabel(u);
            });
          },
          function (response) {
            // TODO
          }
        );
      }

      loadGroups();

      function loadDoiServers() {
        $scope.doiServerSelected = null;
        if ($scope.gnDoiServerEdit) {
          $scope.gnDoiServerEdit.$setPristine();
        }

        $http.get("../api/doiservers").then(function (response) {
          $scope.doiServers = response.data;
        });
      }

      $scope.updateDoiServerUrl = function (newUrl, urlPrefix) {
        $scope.doiServerSelected.url = newUrl;
        $scope.doiServerSelected.publicUrl = urlPrefix;

        $scope.gnDoiServerEdit.$setDirty();
      };

      $scope.updatingDoiServer = function () {
        $scope.doiServerUpdated = true;
      };

      $scope.selectDoiServer = function (v) {
        if ($scope.gnDoiServerEdit.$dirty) {
          if (!confirm($translate.instant("formConfirmExit"))) {
            return;
          }
        }

        $scope.isUpdate = true;
        $scope.doiServerUpdated = false;
        $scope.doiServerSelected = v;
        $scope.selectedRecordGroups = [];

        for (var i = 0; i < v.publicationGroups.length; i++) {
          var group = _.find($scope.recordGroups, { id: v.publicationGroups[i] });
          if (group !== undefined) {
            $scope.selectedRecordGroups.push(group);
          }
        }

        $scope.gnDoiServerEdit.$setPristine();
      };

      $scope.addDoiServer = function () {
        $scope.isUpdate = false;
        $scope.selectedRecordGroups = [];
        $scope.doiServerSelected = {
          id: "",
          name: "",
          description: "",
          url: "",
          username: "",
          password: "",
          landingPageTemplate:
            "http://localhost:8080/geonetwork/srv/resources/records/{{uuid}}",
          publicUrl: "",
          pattern: "{{uuid}}",
          prefix: "",
          publicationGroups: []
        };
      };
      $scope.saveDoiServer = function () {
        $scope.doiServerSelected.publicationGroups = _.map(
          $scope.selectedRecordGroups,
          "id"
        );

        $http
          .put(
            "../api/doiservers" +
              ($scope.isUpdate ? "/" + $scope.doiServerSelected.id : ""),
            $scope.doiServerSelected
          )
          .then(
            function (response) {
              loadDoiServers();
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("doiServerUpdated"),
                timeout: 2,
                type: "success"
              });
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("doiServerUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      $scope.resetPassword = null;
      $scope.resetUsername = null;
      $scope.resetDoiServerPassword = function () {
        $scope.resetPassword = null;
        $scope.resetUsername = null;
        $("#passwordResetModal").modal();
      };

      $scope.saveNewPassword = function () {
        var data = $.param({
          username: $scope.resetUsername,
          password: $scope.resetPassword
        });

        $http
          .post("../api/doiservers/" + $scope.doiServerSelected.id + "/auth", data, {
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
          })
          .then(
            function (response) {
              $scope.resetPassword = null;
              $("#passwordResetModal").modal("hide");
            },
            function (response) {
              // TODO
            }
          );
      };

      $scope.deleteDoiServerConfig = function () {
        $("#gn-confirm-remove-doiserver").modal("show");
      };

      $scope.confirmDeleteDoiServerConfig = function () {
        $http.delete("../api/doiservers/" + $scope.doiServerSelected.id).then(
          function (response) {
            loadDoiServers();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("doiServerDeleteError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };
      loadDoiServers();
    }
  ]);
})();
