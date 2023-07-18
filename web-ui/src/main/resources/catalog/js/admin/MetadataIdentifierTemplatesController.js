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
  goog.provide("gn_metadata_identifier_templates_controller");

  var module = angular.module("gn_metadata_identifier_templates_controller", []);

  /**
   * GnMetadataIdentifierTemplatesController provides management interface
   * for metadata identifier templates.
   *
   */
  module.controller("GnMetadataIdentifierTemplatesController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",

    function ($scope, $http, $rootScope, $translate) {
      $scope.$on("$locationChangeStart", function (event) {
        if (
          $(".ng-dirty").length > 0 &&
          !confirm($translate.instant("unsavedChangesWarning"))
        )
          event.preventDefault();
      });

      $scope.isNew = false;
      $scope.mdIdentifierTemplates = [];
      $scope.mdIdentifierTemplateSelected = {};

      $scope.selectTemplate = function (template) {
        $scope.isNew = false;

        if ($(".ng-dirty").length > 0 && confirm($translate.instant("doSaveConfirm"))) {
          $scope.saveMetadataIdentifierTemplate(false);
        }
        $scope.mdIdentifierTemplateSelected = template;
        $(".ng-dirty").removeClass("ng-dirty");
      };

      /**
       * Load metadata identifier templates into an array.
       *
       */
      function loadMetadataUrnTemplates() {
        $scope.mdIdentifierTemplateSelected = {};

        $http.get("../api/identifiers?userDefinedOnly=true").then(function (response) {
          $scope.mdIdentifierTemplates = response.data;
        });
      }

      $scope.addMetadataIdentifierTemplate = function () {
        $scope.isNew = true;
        $scope.mdIdentifierTemplateSelected = {
          id: "-99",
          name: "",
          template: ""
        };
      };

      $scope.deleteTemplateConfig = function () {
        $("#gn-confirm-remove-metadataidentifiertpl").modal("show");
      };

      $scope.confirmDeleteTemplateConfig = function () {
        $http.delete("../api/identifiers/" + $scope.mdIdentifierTemplateSelected.id).then(
          function (response) {
            $(".ng-dirty").removeClass("ng-dirty");
            loadMetadataUrnTemplates();
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("metadataIdentifierTemplateDeleted"),
              timeout: 2,
              type: "success"
            });
          },
          function (response) {
            $(".ng-dirty").removeClass("ng-dirty");
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("metadataIdentifierTemplateDeletedError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.saveMetadataIdentifierTemplate = function () {
        $http
          .put(
            "../api/identifiers" +
              ($scope.mdIdentifierTemplateSelected.id !== "-99"
                ? "/" + $scope.mdIdentifierTemplateSelected.id
                : ""),
            $scope.mdIdentifierTemplateSelected
          )
          .then(
            function (response) {
              $(".ng-dirty").removeClass("ng-dirty");
              loadMetadataUrnTemplates();
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("metadataIdentifierTemplateUpdated"),
                timeout: 2,
                type: "success"
              });
            },
            function (response) {
              $(".ng-dirty").removeClass("ng-dirty");
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("metadataIdentifier TemplateUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      loadMetadataUrnTemplates();
    }
  ]);
})();
