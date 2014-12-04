(function() {
  goog.provide('gn_facets_directive');

  goog.require('gn_facet_service');
  var module = angular.module('gn_facets_directive',
      ['gn_facet_service']);


  module.directive('gnFacet', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        require: '^ngSearchForm',
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'facet-item.html',
        scope: {
          facetResults: '=gnFacet',
          facet: '@',
          indexKey: '@',
          currentFacets: '='
        },
        link: function(scope, element, attrs, controller) {

          var initialMaxItems = 5;

          scope.add = function(f, reset) {
            gnFacetService.add(scope.currentFacets, scope.indexKey,
                f['@name'], f['@label']);
            controller.resetPagination();
            controller.triggerSearch();
          };
          scope.initialMaxItems = initialMaxItems;
          scope.maxItems = initialMaxItems;
          scope.toggle = function() {
            scope.maxItems = (scope.maxItems == Infinity) ?
                initialMaxItems : Infinity;
          };
        }
      };
    }]);
  module.directive('gnFacetList', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'facet-list.html',
        scope: {
          facets: '=gnFacetList',
          facetConfig: '=',
          currentFacets: '='
        }
      };
    }]);

  module.directive('gnFacetBreadcrumb', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        replace: true,
        scope: true,
        require: '^ngSearchForm',
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'facet-breadcrumb.html',
        link: function(scope, element, attrs, controller) {
          scope.remove = function(f) {
            gnFacetService.remove(scope.currentFacets, f);
            controller.resetPagination();
            controller.triggerSearch();
          };
        }
      };
    }]);
})();
