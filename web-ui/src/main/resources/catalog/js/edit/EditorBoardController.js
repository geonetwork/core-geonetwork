(function() {
  goog.provide('gn_editorboard_controller');

  var module = angular.module('gn_editorboard_controller',
      []);

  module.controller('GnEditorBoardController', [
    '$scope',
    '$location',
    function($scope, $location) {
      $scope.params = {};
      $scope.onMdClick = function(md) {
        $location.path('/metadata/' + md['geonet:info'].id);
      };

      $scope.$watch('user.id', function(val) {
        if (val) {
          $scope.params['_owner'] = val;
          $scope.$broadcast('resetSearch', $scope.params);
        }
      });
    }
  ]);
})();
