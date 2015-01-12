(function() {
  goog.provide('gn_search_form_controller');










  goog.require('gn_catalog_service');
  goog.require('gn_facets_directive');
  goog.require('gn_search_form_results_directive');
  goog.require('gn_selection_directive');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_facets_directive',
    'gn_selection_directive',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  var searchFormController =
      function($scope, $location, gnSearchManagerService,
               gnFacetService, Metadata) {
    var defaultParams = {
      fast: 'index',
      _content_type: 'json'
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
    $scope.paginationInfo = $scope.paginationInfo || {};

    /**
     * Tells if there is a pagination directive nested to this one.
     * Mainly activated by pagination directive link function.
     */
    $scope.hasPagination = false;
    this.activatePagination = function() {
      $scope.hasPagination = true;
      if (!$scope.searchObj.permalink || (
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

    var cleanSearchParams = function(params) {
      for (v in params) {
        if (params[v] == '') {
          delete params[v];
        }
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
    this.triggerSearchFn = function(keepPagination) {

      $scope.searching++;
      angular.extend($scope.searchObj.params, defaultParams);

      //Close metadata views if some are opened
      $scope.$broadcast('closeMdView');

      // Set default pagination if not set
      if ((!keepPagination &&
          !$scope.searchObj.permalink) ||
          (angular.isUndefined($scope.searchObj.params.from) ||
          angular.isUndefined($scope.searchObj.params.to))) {
        self.resetPagination();
      }

      // Set default sortBy
      if (angular.isUndefined($scope.searchObj.params.sortBy)) {
        angular.extend($scope.searchObj.params, $scope.searchObj.sortbyDefault);
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
            for (var i = 0; i < data.metadata.length; i++) {
              $scope.searchResults.records.push(new Metadata(data.metadata[i]));
            }
            $scope.searchResults.count = data.count;
            $scope.searchResults.facet = data.facet;

            // compute page number for pagination
            if ($scope.searchResults.records.length > 0 &&
                $scope.hasPagination) {

              var paging = $scope.paginationInfo;

              // Means the `from` and `to` params come from permalink
              if ((paging.currentPage - 1) *
                  paging.hitsPerPage + 1 != params.from) {
                paging.currentPage = (params.from - 1) / paging.hitsPerPage + 1;
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
              paging.from = (paging.currentPage - 1) * paging.hitsPerPage + 1;
            }
          });
    };

    /**
     * If we use permalink, the triggerSerach call will in fact just update
     * the url with the params, then the event $locationChangeSuccess will call
     * the geonetwork search from url params.
     */
    if ($scope.searchObj.permalink) {
      var triggerSearchFn = self.triggerSearchFn;
      var facetsParams;

      self.triggerSearch = function(keepPagination, initial) {
        $scope.initial = !!initial;
        if (!keepPagination) {
          self.resetPagination();
        }

        facetsParams = gnFacetService.getParamsFromFacets($scope.currentFacets);
        $scope.$broadcast('beforesearch');
        var params = angular.copy($scope.searchObj.params);
        cleanSearchParams(params);
        angular.extend(params, facetsParams);

        if (angular.equals(params, $location.search())) {
          triggerSearchFn(false);
        } else {
          $location.search(params);
        }
      };

      $scope.$on('$locationChangeSuccess', function() {
        var params = angular.copy($location.search());
        for (var o in facetsParams) {
          delete params[o];
        }

        // Take into account only search parameters.
        //
        // TODO: 2 options
        // 1) prefix search parameters by the form id (eg. in Ext.js
        // we used to have "s_"
        // 2) use a single parameter which contains the query
        // eg. q=_cat:"applications"
        // 3) Keep only parameters for search parameters. Other params
        // will be before the #
        //
        // For the time being, drop the tab parameter
        // which defines the tab to open.
        // This allows to open a search with the search
        // tab on catalog.search#?tab=search&_cat=applications
        delete params.tab;

        $scope.searchObj.params = params;
        triggerSearchFn();
      });
    }
    else {
      this.triggerSearch = this.triggerSearchFn;
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
      if ($scope.searchObj.sortbyDefault) {
        angular.extend($scope.searchObj.params, $scope.searchObj.sortbyDefault);
      }

      self.resetPagination();
      $scope.currentFacets = [];
      $scope.triggerSearch();
      $scope.$broadcast('resetSelection');
    };
    $scope.$on('resetSearch', function(evt, searchParams) {
      $scope.controller.resetSearch(searchParams);
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

          scope.resetSearch = function(htmlQuery) {
            scope.controller.resetSearch();
            //TODO: remove geocat ref
            $('.geocat-search').find('.bootstrap-tagsinput .tag').remove();
            if (htmlQuery) {
              $(htmlQuery).focus();
            }
          };

          if (attrs.runsearch) {

            // get permalink params on page load
            if (scope.searchObj.permalink) {
              angular.extend(scope.searchObj.params, $location.search());
            }

            var initial = jQuery.isEmptyObject(scope.searchObj.params);

            // wait for pagination to be set before triggering search
            if (element.find('[data-gn-pagination]').length > 0) {
              var unregisterFn = scope.$watch('hasPagination', function() {
                if (scope.hasPagination) {
                  scope.triggerSearch(true, initial);
                  unregisterFn();
                }
              });
            } else {
              scope.triggerSearch(false, initial);
            }
          }
        }
      };
    }]);
})();
