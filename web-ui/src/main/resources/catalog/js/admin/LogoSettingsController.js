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
  goog.provide("gn_logo_settings_controller");

  var module = angular.module("gn_logo_settings_controller", ["blueimp.fileupload"]);

  /**
   * GnLogoSettingsController provides management interface
   * for catalog logo and harvester logo.
   *
   */
  module.controller("GnLogoSettingsController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    function ($scope, $http, $rootScope, $translate) {
      /**
       * The list of catalog logos
       */
      $scope.logos = [];

      /**
       * Load list of logos
       */
      loadLogo = function () {
        $scope.logos = [];
        $http.get("../api/logos").then(function (response) {
          $scope.logos = response.data;
        });
      };

      /**
       * Callback when error uploading file.
       */
      loadLogoError = function (e, data) {
        if (data.jqXHR.status !== 201) {
          $rootScope.$broadcast("StatusUpdated", {
            title: $translate.instant("logoUploadError"),
            error: data.jqXHR.responseJSON,
            timeout: 0,
            type: "danger"
          });
        } else {
          loadLogo();
        }
      };

      /**
       * Configure logo uploader
       */
      $scope.logoUploadOptions = {
        autoUpload: true,
        done: loadLogo,
        fail: loadLogoError
      };

      /**
       * Set the catalog logo and optionnaly the favicon
       * if favicon parameter is set to true.
       */
      $scope.setCatalogLogo = function (logoName, asFavicon) {
        $http.put("../api/site/logo?file=" + logoName + "&asFavicon=" + asFavicon).then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("logoUpdated"),
              timeout: 2,
              type: "success"
            });
            $rootScope.$broadcast("loadCatalogInfo");
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("logoUpdateError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
            loadLogo();
          }
        );
      };

      /**
       * Ask for confirmation to delete a logo
       */
      $scope.removeLogo = function (logoName) {
        $scope.remLogoName = logoName;
        $("#gn-confirm-remove-logo").modal("show");
      };

      /**
       * Remove the logo and refresh the list when done.
       */
      $scope.confirmRemoveLogo = function (logoName) {
        $http.delete("../api/logos/" + $scope.remLogoName).then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("logoRemoved"),
              timeout: 2,
              type: "success"
            });
            loadLogo();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("logoRemoveError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
            loadLogo();
          }
        );
      };

      $scope.filterLogoList = function (e, formId) {
        var filterValue = e.target.value.toLowerCase();

        $(formId + " .list-group-item").filter(function () {
          var filterText = $(this).find("label").text().toLowerCase();
          var matchStart = filterText.indexOf("" + filterValue.toLowerCase() + "");

          if (matchStart > -1) {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      };

      $scope.resetFilter = function (formId) {
        $(formId + " .list-group-item").each(function () {
          // clear filter
          $("#filter-settings").val("");
          // show the element
          $(this).show();
        });
      };

      /**
       * Toggle the logo height
       * @param  {String} type Type of logo height selected
       */
      $scope.toggleLogoHeight = function (type) {
        $scope.logoheightType = type;
      };

      loadLogo();
    }
  ]);
})();
