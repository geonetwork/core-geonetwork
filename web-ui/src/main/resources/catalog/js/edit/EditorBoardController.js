(function() {
  goog.provide('gn_editorboard_controller');

  var module = angular.module('gn_editorboard_controller',
      []);

  module.controller('GnEditorBoardController', [
    '$scope',
    function($scope) {
      $scope.params = {};

      $scope.$watch('user.id', function(val) {
        if (val) {
          $scope.params['_owner'] = val;
          $scope.$broadcast('resetSearch', $scope.params);
        }
      });
    }
  ]);
})();
