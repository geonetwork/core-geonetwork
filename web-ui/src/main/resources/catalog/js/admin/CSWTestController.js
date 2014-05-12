(function() {
  goog.provide('gn_csw_test_controller');


  var module = angular.module('gn_csw_test_controller',
      []);


  /**
   * GnCSWTestController provides simple testing
   * of CSW service
   *
   */
  module.controller('GnCSWTestController', [
    '$scope', '$http',
    function($scope, $http) {

      /**
       * CSW tests
       */
      $scope.cswTests = {};
      $scope.currentTestId = null;
      $scope.currentTest = null;
      $scope.currentTestResponse = null;
      $scope.cswUrl = 'csw';

      function loadCSWTest() {
        $http.get('../../xml/csw/test/csw-tests.json').success(function(data) {
          $scope.cswTests = data;
        });
      }

      $scope.$watch('currentTestId', function() {
        if ($scope.currentTestId !== null) {
          $http.get('../../xml/csw/test/' + $scope.currentTestId + '.xml')
          .success(function(data) {
                $scope.currentTest = data;
                $scope.runCSWRequest();
              });
        }
      });

      $scope.runCSWRequest = function() {
        $scope.currentTestResponse = '';
        $http.post($scope.cswUrl, $scope.currentTest, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          $scope.currentTestResponse = data;
        });
      };

      loadCSWTest();

    }]);

})();
