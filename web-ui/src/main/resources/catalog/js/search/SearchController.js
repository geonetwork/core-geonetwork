(function() {
  goog.provide('gn_search_controller');


  var module = angular.module('gn_search_controller',
      []);


  /**
   * GnsearchController provides administration tools
   */
  module.controller('GnSearchController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService) {

      // The pagination config
      $scope.paginationInfo = {
        pages: -1,
        currentPage: 0,
        hitsPerPage: 10
      };
    }]);

})();
