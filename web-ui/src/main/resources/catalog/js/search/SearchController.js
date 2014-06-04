(function() {
  goog.provide('gn_search_controller');


  var module = angular.module('gn_search_controller',
      []);


  /**
   * GnsearchController provides administration tools
   */
  module.controller('GnSearchController', [
    '$scope',
    function($scope) {

      $scope.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };
      $scope.paginationInfo = {
        hitsPerPage: 15
      };
    }]);
})();
