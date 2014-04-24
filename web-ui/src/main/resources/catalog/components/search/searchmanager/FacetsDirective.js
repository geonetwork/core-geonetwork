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
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'facet-item.html',
        scope: {
          facets: '=gnFacet',
          facet: '@',
          indexKey: '@'
        },
        link: function(scope, element, attrs) {
          scope.indexKey = scope.indexKey || scope.facet;
          scope.add = function(f, reset) {
            gnFacetService.add(scope.indexKey, f['@name'], f['@label']);
            return false;
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
          facets: '=gnFacetList'
        },
        link: function(scope, element, attrs) {

        }
      };
    }]);
  module.directive('gnFacetBreadcrumb', [
    'gnFacetService', 'gnCurrentFacet',
    function(gnFacetService, gnCurrentFacet) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'facet-breadcrumb.html',
        link: function(scope, element, attrs) {
          scope.currentFacet = gnCurrentFacet.facets;
          scope.remove = function(f) {
            gnFacetService.remove(f);
            return false;
          };
        }
      };
    }]);
})();
