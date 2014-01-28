(function() {
  goog.provide('gn_search_form_controller');




  goog.require('gn_catalog_service');
  goog.require('gn_search_form_results_directive');
  goog.require('gn_urlutils_service');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_urlutils_service',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  module.controller('GnSearchFormController', [
    '$scope',
    'gnSearchManagerService',
    function($scope, gnSearchManagerService) {
      var defaultServiceUrl = 'qi@json';
      var defaultParams = {
        fast: 'index'
      };
      $scope.searchResults = {
        records: [],
        count: 0
      };
      $scope.paginationInfo = null;

      /**
       * If an object {paginationInfo} is defined inside the
       * SearchFormController, then add from and to  params
       * to the search.
       */
      var getPaginationParams = function() {
        pageOptions = $scope.paginationInfo;
        return {
          from: (pageOptions.currentPage - 1) * pageOptions.hitsPerPage + 1,
          to: pageOptions.currentPage * pageOptions.hitsPerPage
        };
      };

      /**
       * Trigger a search with all params contained
       * in $scope.params (updated with defaultParams
       * and pagination params).
       */
      $scope.triggerSearch = function() {

        angular.extend($scope.params, defaultParams);

        // If pagination defined
        // If not, set from and to in params
        // or let default server side values apply.
        if ($scope.paginationInfo) {
          angular.extend($scope.params, getPaginationParams());
        }
        gnSearchManagerService.gnSearch($scope.params).then(
            function(data) {
              $scope.searchResults.records = data.metadata;
              $scope.searchResults.count = data.count;
            });
      };

      /**
       * Clear search results.
       */
      $scope.clearResults = function() {
        $scope.searchResults = {
          records: [],
          count: 0
        };
      };

      // trigger search on pagination events
      $scope.$watch('paginationInfo.currentPage', function() {
        if ($scope.paginationInfo &&
            $scope.paginationInfo.pages > 0) {
          $scope.triggerSearch();
        }
      });


      $scope.$on('resetSearch', function(evt, searchParams) {
        if (searchParams) {
          $scope.params = searchParams;
        } else {
          $scope.params = {};
        }
        if ($scope.paginationInfo) {
          $scope.paginationInfo.currentPage = 1;
        }
        $scope.triggerSearch();
        $scope.$broadcast('resetSelection');
      });
    }
  ]);
})();
