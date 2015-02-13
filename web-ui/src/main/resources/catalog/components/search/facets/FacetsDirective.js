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
    'gnFacetConfigService',
    function(gnFacetConfigService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-list.html',
        scope: {
          facets: '=gnFacetList',
          summaryType: '=facetConfig',
          currentFacets: '='
        },
        link: function(scope) {
          scope.facetConfig = [];
          gnFacetConfigService.loadConfig(scope.summaryType).
              then(function(data) {
                scope.facetConfig = data;
              });
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
    'gnFacetConfigService',
    function(gnFacetService, gnFacetConfigService) {


      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-multiselect.html',
        scope: true,
        link: function(scope, element, attrs) {

          var delimiter = ' or ';
          var oldParams;

          scope.name = attrs.gnFacetMultiselect;

          gnFacetConfigService.loadConfig('hits').then(function(data) {
            if (angular.isArray(data)) {
              for (var i = 0; i < data.length; i++) {
                if (data[i].name == scope.name) {
                  scope.facetConfig = data[i];
                  break;
                }
              }
            }
          }).then(function() {
            scope.$watch('searchResults.facet', function(v) {
              /*
              if (oldParams &&
                  oldParams != scope.searchObj.params[scope.facetConfig.key]) {
              }
              else if (v) {
                oldParams = scope.searchObj.params[scope.facetConfig.key];
                scope.facetObj = v[scope.facetConfig.label];
              }
              */
              if (v && scope.facetConfig && scope.facetConfig.label) {
                scope.facetObj = v[scope.facetConfig.label];
              }
            });
          });


          /**
           * Check if the facet item is checked or not, depending if the
           * value is in the search params.
           * @param {string} value
           * @return {*|boolean}
           */
          scope.isInSearch = function(value) {
            return scope.searchObj.params[scope.facetConfig.key] &&
                scope.searchObj.params[scope.facetConfig.key].split(delimiter).
                    indexOf(value) >= 0;
          };

          //TODO improve performance here, maybe to complex $watchers
          // add subdirective to watch a boolean and make only one
          // watcher on searchObj.params
          scope.updateSearch = function(value, e) {
            var search = scope.searchObj.params[scope.facetConfig.key];
            if (angular.isUndefined(search)) {
              scope.searchObj.params[scope.facetConfig.key] = value;
            }
            else {
              if (search == '') {
                scope.searchObj.params[scope.facetConfig.key] = value;
              }
              else {
                var s = search.split(delimiter);
                var idx = s.indexOf(value);
                if (idx < 0) {
                  scope.searchObj.params[scope.facetConfig.key] +=
                      delimiter + value;
                }
                else {
                  s.splice(idx, 1);
                  scope.searchObj.params[scope.facetConfig.key] =
                      s.join(delimiter);
                }
              }
            }
            scope.$emit('resetSearch', scope.searchObj.params);
            e.preventDefault();
          };
        }
      };
    }]);
})();
