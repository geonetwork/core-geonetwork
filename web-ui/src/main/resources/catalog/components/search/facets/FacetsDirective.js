(function() {
  goog.provide('gn_facets_directive');

  var module = angular.module('gn_facets_directive', []);

  module.directive('gnFacet', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        require: '^ngSearchForm',
        replace: true,
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-item.html',
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
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-list.html',
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
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-breadcrumb.html',
        link: function(scope, element, attrs, controller) {
          scope.remove = function(f) {
            gnFacetService.remove(scope.currentFacets, f);
            controller.resetPagination();
            controller.triggerSearch();
          };
        }
      };
    }]);

  module.directive('gnFacetMultiselect', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        replace: true,
        require: '^ngSearchForm',
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-multiselect.html',
        scope: true,
        link: function(scope, element, attrs, controller) {

          var delimiter = ' or ';
          scope.field = attrs.gnFacetMultiselect;
          scope.index = scope.field.substring(0, scope.field.length - 1);

          scope.$watch('searchResults.facet', function(v) {
            scope.facetObj = v[scope.field];
          });

          // Manage elements displayed
          var initialMaxItems = 5;
          scope.initialMaxItems = initialMaxItems;
          scope.maxItems = initialMaxItems;
          scope.toggle = function() {
            scope.maxItems = (scope.maxItems == Infinity) ?
                initialMaxItems : Infinity;
          };

          /**
           * Check if the facet item is checked or not, depending if the
           * value is in the search params.
           * @param {string} value
           * @return {*|boolean}
           */
          scope.isInSearch = function(value) {
            return scope.searchObj.params[scope.index] &&
                scope.searchObj.params[scope.index].split(delimiter).
                    indexOf(value) >= 0;
          };

          //TODO improve performance here, maybe to complex $watchers
          // add subdirective to watch a boolean and make only one
          // watcher on searchObj.params
          scope.updateSearch = function(value) {
            var search = scope.searchObj.params[scope.index];
            if (angular.isUndefined(search)) {
              scope.searchObj.params[scope.index] = value;
            }
            else {
              if (search == '') {
                scope.searchObj.params[scope.index] = value;
              }
              else {
                var s = search.split(delimiter);
                var idx = s.indexOf(value);
                if (idx < 0) {
                  scope.searchObj.params[scope.index] += delimiter + value;
                }
                else {
                  s.splice(idx, 1);
                  scope.searchObj.params[scope.index] = s.join(delimiter);
                }
              }
            }
            scope.$emit('resetSearch', scope.searchObj.params);
          };

          scope.remove = function(f) {
            gnFacetService.remove(scope.currentFacets, f);
            controller.resetPagination();
            controller.triggerSearch();
          };
        }
      };
    }]);
})();
