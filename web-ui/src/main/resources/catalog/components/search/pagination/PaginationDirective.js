(function() {
  goog.provide('gn_pagination_directive');

  var module = angular.module('gn_pagination_directive', []);

  module.directive('gnPagination', [
                                    function() {

      return {
        restrict: 'A',
        replace: true,
        require: '^ngSearchForm',
        scope: {
          config: '=gnPagination'
        },
        templateUrl: '../../catalog/components/search/pagination/partials/' +
            'pagination.html',
        link: function(scope, element, attrs, controller) {

          // Init config from default and eventual given one
          var defaultConfig = {
            pages: -1,
            currentPage: 1,
            hitsPerPage: 10
          };
          angular.extend(defaultConfig, scope.config);
          scope.config = defaultConfig;
          delete defaultConfig;

          /**
           * If an object {paginationInfo} is defined inside the
           * SearchFormController, then add from and to  params
           * to the search.
           */
          var getPaginationParams = function() {
            var pageOptions = scope.config;
            return {
              from: (pageOptions.currentPage - 1) * pageOptions.hitsPerPage + 1,
              to: pageOptions.currentPage * pageOptions.hitsPerPage
            };
          };
          controller.getPaginationParams = getPaginationParams;

          var updateSearch = function() {
            controller.updateSearchParams(getPaginationParams());
            controller.triggerSearch();
          };

          scope.previous = function() {
            if (scope.config.currentPage > 1) {
              scope.config.currentPage -= 1;
              updateSearch();
            }
          };
          scope.next = function() {
            if (scope.config.currentPage < scope.config.pages) {
              scope.config.currentPage += 1;
              updateSearch();
            }
          };
          controller.updateSearchParams(getPaginationParams());
          controller.activatePagination();
        }
      };
    }]);
})();
