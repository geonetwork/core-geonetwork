(function() {
  goog.provide('gn_search_form_controller');






  goog.require('gn_catalog_service');
  goog.require('gn_facet_service');
  goog.require('gn_search_form_results_directive');
  goog.require('gn_urlutils_service');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_facet_service',
    'gn_urlutils_service',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  module.controller('GnSearchFormController', [
    '$scope',
    'gnSearchManagerService',
    'gnCurrentFacet',
    function($scope, gnSearchManagerService, gnCurrentFacet) {
      var defaultServiceUrl = 'qi@json';
      var defaultParams = {
        fast: 'index'
      };
      $scope.searchResults = {
        records: [],
        count: 0
      };
      $scope.paginationInfo = null;
      $scope.currentFacet = gnCurrentFacet;
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
              $scope.searchResults.facet = data.facet;

              // Event on new search result
              // compute page number for pagination
              if ($scope.searchResults.records.length > 0) {
                $scope.paginationInfo.pages = Math.ceil(
                    $scope.searchResults.count /
                    $scope.paginationInfo.hitsPerPage, 0);
              }
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
      $scope.$on('setPagination', function(evt, pagination) {
        $scope.paginationInfo = pagination;
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
      $scope.$on('clearResults', function() {
        $scope.clearResults();
      });
      $scope.$watchCollection('currentFacet', function() {
        if (gnCurrentFacet.facets) {
          // Drop delete facets from params
          angular.forEach(gnCurrentFacet.deletedFacets, function(value, key) {
            delete $scope.params[key];
            delete gnCurrentFacet.deletedFacets[key];
          });
          // Add new facets
          angular.forEach(gnCurrentFacet.facets, function(facet, key) {
            $scope.params[key] = facet.value;
          });
          $scope.triggerSearch();
        }
      });

    }
  ]);
})();
