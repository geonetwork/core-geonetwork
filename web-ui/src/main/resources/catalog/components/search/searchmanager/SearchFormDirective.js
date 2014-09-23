(function() {
  goog.provide('gn_search_form_controller');






  goog.require('gn_catalog_service');
  goog.require('gn_facets_directive');
  goog.require('gn_selection_directive');
  goog.require('gn_search_form_results_directive');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_facets_directive',
    'gn_selection_directive',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  var searchFormController = function($scope, $location,
                                      gnSearchManagerService, gnFacetService, Metadata) {
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

    $scope.searching = 0;

    /**
     * Tells if there is a pagination directive nested to this one.
     * Mainly activated by pagination directive link function.
     */
    $scope.hasPagination = false;
    this.activatePagination = function() {
      $scope.hasPagination = true;
      if(!$scope.searchObj.permalink || (
          angular.isUndefined($scope.searchObj.params.from) &&
          angular.isUndefined($scope.searchObj.params.to)
          )) {
        self.resetPagination();
      }
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
     *
     * @param {boolean} resetPagination If true, then
     * don't reset pagination info.
     */
    this.triggerSearch = function(keepPagination) {

      $scope.searching++;
      angular.extend($scope.searchObj.params, defaultParams);

      if(!keepPagination && !$scope.searchObj.permalink) {
        self.resetPagination();
      }

      // Don't add facet extra params to $scope.params but
      // compute them each time on a search.
      var params = angular.copy($scope.searchObj.params);
      if ($scope.currentFacets.length > 0) {
        angular.extend(params,
            gnFacetService.getParamsFromFacets($scope.currentFacets));
      }

      gnSearchManagerService.gnSearch(params).then(
          function(data) {
            $scope.searching--;
            $scope.searchResults.records = [];
            for(var i=0;i<data.metadata.length;i++) {
              $scope.searchResults.records.push(new Metadata(data.metadata[i]));
            }
            $scope.searchResults.count = data.count;
            $scope.searchResults.facet = data.facet;

            // compute page number for pagination
            if ($scope.searchResults.records.length > 0 && $scope.hasPagination) {

              var paging = $scope.paginationInfo;

              // Means the `from` and `to` params come from permalink
              if((paging.currentPage-1)*paging.hitsPerPage+1 != params.from) {
                paging.currentPage = (params.from-1) / paging.hitsPerPage +1;
              }

              paging.resultsCount = $scope.searchResults.count;
              paging.to = Math.min(
                  data.count,
                  paging.currentPage * paging.hitsPerPage
              );
              paging.pages = Math.ceil(
                  $scope.searchResults.count /
                  paging.hitsPerPage, 0
              );
              paging.from = (paging.currentPage-1)*paging.hitsPerPage+1;
            }
          });
    };

    if($scope.searchObj.permalink) {
      var triggerSearchFn = self.triggerSearch;
      var init = false; // Avoid the first $locationChangeSuccess event
      var facetsParams;

      self.triggerSearch = function(keepPagination) {
        if(!keepPagination) {
          self.resetPagination();
        }

        facetsParams = gnFacetService.getParamsFromFacets($scope.currentFacets);
        var params = angular.copy($scope.searchObj.params);
        angular.extend(params, facetsParams);

        if(angular.equals(params, $location.search())) {
          triggerSearchFn();
        }
        else {
          $location.search(params);
        }
        init = true;
      };

      $scope.$on('$locationChangeSuccess', function () {
        if(init) {
          var params = angular.copy($location.search());
          for(var o in facetsParams) {
            delete params[o];
          }
          $scope.searchObj.params = params;
          triggerSearchFn();
        }
      });
    }

    /**
     * update $scope.params by merging it with given params
     * @param {!Object} params
     */
    this.updateSearchParams = function(params) {
      angular.extend($scope.searchObj.params, params);
    };

    this.resetSearch = function(searchParams) {
      if (searchParams) {
        $scope.searchObj.params = searchParams;
      } else {
        $scope.searchObj.params = {};
      }
      self.resetPagination();
      $scope.currentFacets = [];
      $scope.triggerSearch();
      $scope.$broadcast('resetSelection');
    };
    $scope.$on('resetSearch', function(evt, searchParams) {
      resetSearch(searchParams);
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
    '$location',
    'gnSearchManagerService',
    'gnFacetService',
    'Metadata'
  ];

  module.directive('ngSearchForm', [
    '$location',
    function($location) {
      return {
        restrict: 'A',
        scope: true,
        controller: searchFormController,
        controllerAs: 'controller',
        link: function(scope, element, attrs) {

          scope.resetSearch = function() {
            scope.controller.resetSearch();
            $('.geocat-search').find('.bootstrap-tagsinput .tag').remove();
          };

          if (attrs.runsearch) {

            // get permalink params on page load
            if(scope.searchObj.permalink) {
              angular.extend(scope.searchObj.params, $location.search());
            }

            // wait for pagination to be set before triggering search
            if (element.find('[data-gn-pagination]').length > 0) {
              var unregisterFn = scope.$watch('hasPagination', function() {
                if (scope.hasPagination) {
                  scope.triggerSearch(true);
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
