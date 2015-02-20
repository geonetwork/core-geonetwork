(function() {
  goog.provide('gn_import_controller');

  goog.require('gn_category');
  goog.require('gn_importxsl');

  var module = angular.module('gn_import_controller', [
    'gn_importxsl',
    'gn_category',
    'blueimp.fileupload'
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
      $scope.recordTypes = [
        {key: 'METADATA', value: 'n'},
        {key: 'TEMPLATE', value: 'y'},
        {key: 'SUB_TEMPLATE', value: 's'}
      ];

      $scope.template = $scope.recordTypes[0].value;

      /** Upload management */
      $scope.action = 'xml.mef.import.ui';
      var uploadImportMdDone = function(evt, data) {
        $scope.importing = false;
        $scope.report = {
          id: data.jqXHR.responseJSON.id,
          success: data.jqXHR.responseJSON.success,
          message: data.jqXHR.responseJSON.msg
        };
      };
      var uploadImportMdError = function(evt, data, o) {
        $scope.importing = false;
        var response = new DOMParser().parseFromString(
            data.jqXHR.responseText, 'text/xml');
        $scope.report = {
          message: response.getElementsByTagName('message')[0].innerHTML
        };
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
      };
      var onSuccessFn = function(response) {
        $scope.importing = false;
        if (response.data.exceptions) {
          $scope.report = response.data;
          formatExceptionArray();
        } else {
          $scope.report = response.data;
        }
        $scope.report.success = parseInt($scope.report.records) -
            parseInt(($scope.report.exceptions &&
            $scope.report.exceptions['@count']) || 0);
      };
      var onErrorFn = function(error) {
        $scope.importing = false;
        $scope.report = error.data;
        formatExceptionArray();
      };

      $scope.importRecords = function(formId) {
        $scope.report = null;
        $scope.error = null;

        if ($scope.importMode == 'uploadFile') {
          var uploadScope = angular.element('#md-import-file').scope();
          if (uploadScope.queue.length > 0) {
            $scope.importing = true;
            uploadScope.submit();
          }
          else {
            $scope.report = {
              message: 'noFileSelected'
            };
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
