(function() {
  goog.provide('gn_search_controller');


  var module = angular.module('gn_search_controller',
      []);


  /**
   * GnsearchController provides administration tools
   */
  module.controller('GnSearchController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnMetadataManagerService, 
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

      $scope.searchRecordsSearchFor = function(e) {
        $scope.searchRecordsFilter = (e ? e.target.value : '');
        $scope.searchRecordsPagination.currentPage = 0;
        $scope.searchRecordsSearch();
      };

      // Run a search according to paging option
      // This should probably move to some kind of SearchManager module
      // FIXME : can't watch the searchRecordsFilter changes ?
      $scope.searchRecordsSearch = function() {
        var pageOptions = $scope.searchRecordsPagination;

        gnSearchManagerService.search($scope.url + 'q@json?fast=index' +
            '&any=' + $scope.searchRecordsFilter +
            '&from=' + (pageOptions.currentPage *
            pageOptions.hitsPerPage + 1) +
            '&to=' + ((pageOptions.currentPage + 1) *
                          pageOptions.hitsPerPage))
          .then(function(data) {
              $scope.searchRecords = data.metadata;
              $scope.searchRecordsPagination.pages = Math.round(
                  data.count /
                  $scope.searchRecordsPagination.hitsPerPage, 0);
            }, function(data) {
              // TODO
            });
      };
      // When the current page change trigger the search
      $scope.$watch('searchRecordsPagination.currentPage', function() {
        $scope.searchRecordsSearch();
      });

    }]);

})();
