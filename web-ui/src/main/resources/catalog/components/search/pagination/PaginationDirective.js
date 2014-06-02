(function() {
  goog.provide('gn_pagination_directive');

  var module = angular.module('gn_pagination_directive', []);

  module.directive('gnPagination', [
                                    function() {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        require: '^ngSearchForm',
        scope: {
          config: '=gnPagination'
        },
        templateUrl: '../../catalog/components/search/pagination/partials/' +
            'pagination.html',
        link: function(scope, element, attrs, controller) {
          scope.previous = function() {
            if (scope.config.currentPage > 1) {
              scope.config.currentPage -= 1;
            }
          };
          scope.next = function() {
            if (scope.config.currentPage < scope.config.pages) {
              scope.config.currentPage += 1;
            }
          };
        }
      };
    }]);
})();
