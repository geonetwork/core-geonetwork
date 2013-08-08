(function() {
    goog.provide('gn_pagination_directive');

    var module = angular.module('gn_pagination_directive', []);

    module.directive('gnPagination', ['gnMetadataManagerService', function(gnMetadataManagerService) {
        
        return {
            restrict : 'A',
            replace: true,
            transclude: true,
            scope: { 
                config: '=gnPagination'
            },
            templateUrl: '../../catalog/components/pagination/partials/' +
              'pagination.html',
            link : function(scope, element, attrs) {
                scope.previous = function () {
                    if (scope.config.currentPage > 0) {
                        scope.config.currentPage -= 1;
                    }
                }
                scope.next = function () {
                    if (scope.config.currentPage < scope.config.pages) {
                        scope.config.currentPage += 1;
                    }
                }
            }
        };
    }]);
})();
