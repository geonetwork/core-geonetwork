(function() {
  goog.provide('gn_search_form_controller');






  goog.require('gn_catalog_service');
  goog.require('gn_facets_directive');
  goog.require('gn_search_form_results_directive');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_facets_directive',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  var searchFormController = function($scope,
                                      gnSearchManagerService, gnFacetService) {
    var defaultServiceUrl = 'qi@json';
    var defaultParams = {
      fast: 'index'
    };
    var self = this;

    /** State of the facets of the current search */
    $scope.currentFacets = [];

    /** Object were are stored result search information */
    $scope.searchResults = {
      records: [],
      count: 0
    };

    /**
     * Tells if there is a pagination directive nested to this one.
     * Mainly activated by pagination directive link function.
     */
    $scope.hasPagination = false;
    this.activatePagination = function() {
      $scope.hasPagination = true;
    };

    /**
     * Reset pagination 'from' and 'to' params and merge them
     * to $scope.params
     */
    this.resetPagination = function() {
      if ($scope.hasPagination) {
        $scope.paginationInfo.currentPage = 1;
        this.updateSearchParams(this.getPaginationParams());
      }
    };

    /**
     * triggerSearch
     *
     * Run a search with the actual $scope.params
     * merged with the params from facets state.
     * Update the paginationInfo object with the total
     * count of metadata found.
     */
    this.triggerSearch = function() {

      angular.extend($scope.params, defaultParams);

      // Don't add facet extra params to $scope.params but
      // compute them each time on a search.
      var params = angular.copy($scope.params);
      if ($scope.currentFacets.length > 0) {
        angular.extend(params,
            gnFacetService.getParamsFromFacets($scope.currentFacets));
      }

      gnSearchManagerService.gnSearch(params).then(
          function(data) {
            $scope.searchResults.records = data.metadata;
            $scope.searchResults.count = data.count;
            $scope.searchResults.facet = data.facet;

            // compute page number for pagination
            if ($scope.searchResults.records.length > 0 &&
                $scope.hasPagination) {
              $scope.paginationInfo.pages = Math.ceil(
                  $scope.searchResults.count /
                      $scope.paginationInfo.hitsPerPage, 0);
            }
          });
    };

    /**
     * update $scope.params by merging it with given params
     * @param {!Object} params
     */
    this.updateSearchParams = function(params) {
      angular.extend($scope.params, params);
    };

    $scope.$on('resetSearch', function(evt, searchParams) {
      if (searchParams) {
        $scope.params = searchParams;
      } else {
        $scope.params = {};
      }
      self.resetPagination();
      $scope.currentFacets = [];
      $scope.triggerSearch();
      $scope.$broadcast('resetSelection');
    });

    $scope.$on('clearResults', function() {
      $scope.searchResults = {
        records: [],
        count: 0
      };
    });

    $scope.triggerSearch = this.triggerSearch;
  };

  searchFormController['$inject'] = [
    '$scope',
    'gnSearchManagerService',
    'gnFacetService'
  ];

  module.directive('ngSearchForm', [
    '$parse',
    function($parse) {
      return {
        restrict: 'A',
        scope: true,
        controller: searchFormController,
        link: function(scope, element, attrs) {

          // Get search params from parent scope
          scope.params = $parse(attrs.gnParams)(scope) || {};

          if (attrs.runsearch) {
            if (element.find('[data-gn-pagination]').length > 0) {
              var unregisterFn = scope.$watch('hasPagination', function() {
                if (scope.hasPagination) {
                  scope.triggerSearch();
                  unregisterFn();
                }
              });
            } else {
              scope.triggerSearch();
            }
          }
        }
      };
    }]);
})();
