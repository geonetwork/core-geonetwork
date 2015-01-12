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
    function($scope, $http, $compile, gnSearchSettings) {
      $scope.formatter = gnSearchSettings.formatter;
      $scope.usingFormatter = false;
      $scope.compileScope = $scope.$new();

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
