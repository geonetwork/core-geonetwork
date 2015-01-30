(function() {
  goog.provide('gn_mdview');


  goog.require('gn_mdview_directive');
  goog.require('gn_mdview_service');

  var module = angular.module('gn_mdview', [
    'gn_mdview_service',
    'gn_mdview_directive'
  ]);

  module.controller('GnMdViewController', [
    '$scope', '$http', '$compile', 'gnSearchSettings',
    'gnMetadataActions', 'gnAlertService', '$translate',
    function($scope, $http, $compile, gnSearchSettings,
             gnMetadataActions, gnAlertService, $translate) {
      $scope.formatter = gnSearchSettings.formatter;
      $scope.gnMetadataActions = gnMetadataActions;
      $scope.usingFormatter = false;
      $scope.compileScope = $scope.$new();

      $scope.deleteRecord = function(md) {
        gnMetadataActions.deleteMd(md).then(function(data) {
          gnAlertService.addAlert({
            msg: $translate('metadataRemoved',
                {title: md.title || md.defaultTitle}),
            type: 'success'
          });
          $scope.closeRecord(md);
        }, function(reason) {
          // Data needs improvements
          // See https://github.com/geonetwork/core-geonetwork/issues/723
          gnAlertService.addAlert({
            msg: reason.data,
            type: 'danger'
          });
        });
      };
      $scope.format = function(f) {
        $scope.usingFormatter = f !== undefined;
        $scope.currentFormatter = f;
        if (f) {
          $http.get(f.url + $scope.mdView.current.record.getUuid()).then(
              function(response) {
                var snippet = response.data.replace(
                    '<?xml version="1.0" encoding="UTF-8"?>', '');

                $('#gn-metadata-display').find('*').remove();

                $scope.compileScope.$destroy();

                // Compile against a new scope
                $scope.compileScope = $scope.$new();
                var content = $compile(snippet)($scope.compileScope);

                $('#gn-metadata-display').append(content);
              });
        }
      };

      // Reset current formatter to open the next record
      // in default mode.
      $scope.$watch('mdView.current.record', function() {
        $scope.usingFormatter = false;
        $scope.currentFormatter = null;
      });
    }]);

})();
