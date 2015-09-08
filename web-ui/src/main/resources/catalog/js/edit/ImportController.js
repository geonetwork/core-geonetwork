(function() {
  goog.provide('gn_import_controller');




  goog.require('gn_category');
  goog.require('gn_formfields_directive');
  goog.require('gn_importxsl');
  goog.require('gn_formfields_directive');

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
        var report = {
          message: response.getElementsByTagName('message')[0].innerHTML
        };
        $scope.reports.push(report);
      };

      // upload directive options
      $scope.mdImportUploadOptions = {
        autoUpload: false,
        done: uploadImportMdDone,
        fail: uploadImportMdError
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

      $scope.importRecords = function(formId) {
        $scope.reports = [];
        $scope.error = null;

        if ($scope.importMode == 'uploadFile') {
          var uploadScope = angular.element('#md-import-file').scope();
          if (uploadScope.queue.length > 0) {
            $scope.importing = true;
            uploadScope.submit();
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
