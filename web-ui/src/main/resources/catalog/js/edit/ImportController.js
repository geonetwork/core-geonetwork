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

(function() {
  goog.provide('gn_import_controller');


  goog.require('gn_category');
  goog.require('gn_formfields_directive');
  goog.require('gn_importxsl');

  var module = angular.module('gn_import_controller', [
    'gn_importxsl',
    'gn_category',
    'blueimp.fileupload',
    'gn_formfields_directive'
  ]);

  /**
   * Metadata import controller.
   *
   * TODO: Add other type of import
   * TODO: Init form from route parameters
   */
  module.controller('GnImportController', [
    '$scope',
    'gnMetadataManager',
    function($scope, gnMetadataManager) {
      $scope.importMode = 'uploadFile';
      $scope.file_type = 'single';
      $scope.uuidAction = 'nothing';
      $scope.importing = false;

      /** Upload management */
      $scope.action = 'xml.mef.import.ui';
      var uploadImportMdDone = function(evt, data) {
        $scope.importing = false;
        var report = {
          id: data.jqXHR.responseJSON.id,
          uuid: data.jqXHR.responseJSON.uuid,
          success: data.jqXHR.responseJSON.success,
          message: data.jqXHR.responseJSON.msg
        };
        $scope.reports.push(report);
      };
      var uploadImportMdError = function(evt, data, o) {
        $scope.importing = false;
        var response = new DOMParser().parseFromString(
            data.jqXHR.responseText, 'text/xml');
        var msgEl = response.getElementsByTagName('message')[0];
        var report = {
          message: msgEl.innerHTML ? msgEl.innerHTML : msgEl.textContent
        };
        $scope.reports.push(report);
      };

      // upload directive options
      $scope.mdImportUploadOptions = {
        autoUpload: false,
        done: uploadImportMdDone,
        fail: uploadImportMdError,
        singleUpload: true
      };
      /** --- */


      var formatExceptionArray = function() {
        if (!angular.isArray($scope.report.exceptions.exception)) {
          $scope.report.exceptions.exception =
              [$scope.report.exceptions.exception];
        }

        $scope.reports.push($scope.report);
      };
      var onSuccessFn = function(response) {
        $scope.importing = false;
        if (response.data.exceptions) {
          $scope.report = response.data;
          formatExceptionArray();
        } else {
          $scope.reports.push(response.data);
        }
        if (response.data.records) {
          $scope.reports.push({success: parseInt(response.data.records) -
                parseInt((response.data.exceptions &&
                response.data.exceptions['@count']) || 0)});
        }
      };
      var onErrorFn = function(error) {
        $scope.importing = false;
        $scope.reports = [{
          exception: error.data
        }];
      };

      $scope.uploadScope = angular.element('#md-import-file').scope();
      $scope.unsupportedFile = false;
      $scope.$watchCollection('uploadScope.queue', function(n, o) {
        if (n != o && n.length == 1) {
          if (n[0].name.match(/.xml$/i) !== null) {
            $scope.file_type = 'single';
          } else if (
              n[0].name.match(/.zip$/i) !== null ||
              n[0].name.match(/.mef$/i) !== null) {
            $scope.file_type = 'mef';
          } else {
            $scope.unsupportedFile = true;
            return;
          }
        }
        $scope.unsupportedFile = false;
      });
      $scope.importRecords = function(formId) {
        $scope.reports = [];
        $scope.error = null;

        if ($scope.importMode == 'uploadFile') {
          if ($scope.uploadScope.queue.length > 0) {
            $scope.importing = true;
            $scope.uploadScope.submit();
          }
          else {
            $scope.reports = [{
              message: 'noFileSelected'
            }];
          }
        } else if ($scope.importMode == 'importFromDir') {
          $scope.importing = true;
          gnMetadataManager.importFromDir($(formId).serialize()).then(
              onSuccessFn, onErrorFn);
        } else {
          $scope.importing = true;
          gnMetadataManager.importFromXml($(formId).serialize()).then(
              onSuccessFn, onErrorFn);
        }
      };
    }
  ]);
})();
