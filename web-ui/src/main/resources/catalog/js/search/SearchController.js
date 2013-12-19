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

      /**
       * The full text search filter
       */
      $scope.searchRecordsFilter = '';

      /**
       * The list of records to be processed
       */
      $scope.searchRecords = null;

      /**
       * The pagination config
       */
      $scope.searchRecordsPagination = {
        pages: -1,
        currentPage: 0,
        hitsPerPage: 20
      };

      // Register the search results, filter and pager
      // and get the search function back
      searchFn = gnSearchManagerService.register({
        records: 'searchRecords',
        filter: 'searchRecordsFilter',
        pager: 'searchRecordsPagination'
        //              error: function () {console.log('error');},
        //              success: function () {console.log('succ');}
      }, $scope);

      // Update search filter and reset page
      $scope.searchRecordsSearchFor = function(e) {
        $scope.searchRecordsFilter = {any: (e ? e.target.value : '')};
        $scope.searchRecordsPagination.currentPage = 0;
        searchFn();
      };

      // When the current page change trigger the search
      $scope.$watch('searchRecordsPagination.currentPage', function() {
        searchFn();
      });

    }]);

})();
