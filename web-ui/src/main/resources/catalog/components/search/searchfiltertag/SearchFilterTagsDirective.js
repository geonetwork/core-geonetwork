(function() {
  goog.provide('search_filter_tags_directive');

  goog.require('gn_thesaurus_service');

  var EXCLUDED_PARAMS = [
    'bboxes',
    'bucket',
    'editable',
    'fast',
    'from',
    'ownRecords',
    'resultType',
    'sortBy',
    'sortOrder',
    'to',
    '_content_type',
    '_isTemplate',
    '_owner'];

  var module = angular.module('search_filter_tags_directive', ['pascalprecht.translate']);

  module.directive('searchFilterTags',
    ['$location',
      function($location) {
        return {
          restrict: 'EA',
          require: '^ngSearchForm',
          templateUrl: '../../catalog/components/search/searchfiltertag/'+
            'partials/searchFilterTagsTemplate.html',
          scope: {
            dimensions: '=',
            useLocationParameters: '='
          },
          link: function(scope, element, attrs, ngSearchFormCtrl) {
            scope.currentFilters = [];

            // key is the raw facet path, value is a valid filter object
            scope.facetFilterCache = {};

            function getSearchParams() {
              if (scope.useLocationParameters) {
                return $location.search();
              } else {
                return ngSearchFormCtrl.getFinalParams();
              }
            }

            function setSearchParameter(paramKey, paramValue) {
              if (scope.useLocationParameters) {
                $location.search(paramKey, paramValue);
              } else {
                var params = ngSearchFormCtrl.getSearchParams();
                if (!paramValue) {
                  delete params[paramKey];
                } else {
                  params[paramKey] = paramValue;
                }
                ngSearchFormCtrl.triggerSearch();
              }
            }

            function createNewFiltersFromFacets(facetParam) {
              return facetParam.split('&').map(function (raw) {
                if (scope.facetFilterCache[raw]) {
                  return scope.facetFilterCache[raw];
                }

                var queryParts = raw.split('/');
                var dimensionKey = queryParts[0];

                var dimension;
                scope.dimensions.forEach(function (d) {
                  if (d['@name'] === dimensionKey) {
                    dimension = d;
                  }
                });

                var categoryLabel;
                function lookupCategory(subtree, currentQueryPartIndex) {
                  if (!subtree.category) {
                    return false;
                  }
                  subtree.category.forEach(function (c) {
                    if (c['@value'] === decodeURIComponent(queryParts[currentQueryPartIndex])) {
                      currentQueryPartIndex++;
                      if (currentQueryPartIndex === queryParts.length) {
                        categoryLabel = c['@label'];
                        return true;
                      }
                      lookupCategory(c, currentQueryPartIndex);
                    }
                  })
                }

                lookupCategory(dimension, 1);

                var filter = {
                  key: dimension['@label'],
                  value: categoryLabel || decodeURIComponent(queryParts[1]),
                  isFacet: true,
                  facetKey: dimension['@name']
                };
                scope.facetFilterCache[raw] = filter;
                return filter;
              });
            }

            function removeFacet(facetKey, facetQuery) {
              if (!facetQuery) {
                return;
              }
              return facetQuery
                .split('&')
                .filter(function (facet) {
                  return facet.split('/')[0] !== facetKey;
                })
                .join('&');
            }

            function removeAllFilters() {
              var params = getSearchParams();
              scope.currentFilters.forEach(function (filter) {
                if (filter.isFacet) {
                  params['facet.q'] = removeFacet(filter.facetKey, params['facet.q']);
                } else {
                  delete params[filter.key];
                }
              });
              scope.$emit('resetSearch', params, false);
            }

            scope.$watch(function() {
              return getSearchParams();
            }, function(newFilters, oldVal) {
              scope.currentFilters = [];

              for (var filterKey in newFilters) {
                // filter param is excluded from summary
                if (EXCLUDED_PARAMS.indexOf(filterKey) > -1) {
                  continue;
                }

                var value = newFilters[filterKey];

                // value is empty/undefined
                if (!value || typeof value !== 'string') {
                  continue;
                }

                // special logic for facets (decoding facet.q value)
                if (filterKey === 'facet.q') {
                  var facetFilters = createNewFiltersFromFacets(value);
                  Array.prototype.push.apply(scope.currentFilters, facetFilters);
                  continue;
                }

                // special logic for geometry: we assume the filter is a bounding box
                if (filterKey === 'geometry') {
                  scope.currentFilters.push({
                    key: 'geometry',
                    value: 'geometryFilter'
                  });
                  continue;
                }

                // special logic for categories: add cat- prefix to each category for translations
                if (filterKey === '_cat') {
                  scope.currentFilters.push({
                    key: filterKey,
                    value: value
                      .split(' or ')
                      .map(function(val) {
                        return 'cat-' + val;
                      })
                      .join(' or ')
                  });
                  continue;
                }

                // general case
                if (filterKey==="query_string"){
                  angular.forEach(JSON.parse(value), function(facetValues, facetKey) {
                    if (typeof facetValues === 'object') {
                      angular.forEach(facetValues, function (values, facetValuesKey) {
                        var elem = {};
                        elem[facetValuesKey]=values;
                        scope.currentFilters.push({
                          key: facetKey,
                          value: elem
                        })
                      })
                    }
                    else {
                      scope.currentFilters.push({
                        key: facetKey,
                        value: facetValues
                      })
                    }
                  })
                }
                if (scope.isSpecificParameter(filterKey)) {
                  scope.currentFilters.push({
                    key: filterKey,
                    value: value
                  })
                }
              }
            }, true);

            scope.isSpecificParameter = function(filter) {
              // full text search and uuid on selection only
              // are not translated like facet.
              return filter.key === 'any' || filter.key === 'uuid';
            }
            scope.isRange = function(filter) {
              return filter.value && filter.value.match
                  && filter.value.match(/\+\w+:[\[{].* TO .*[\]}]/);
            }

            scope.isNegative = function(value) {
              return value === false
                     || (value && value.match && value.match(/^-\(.*\)$/) != null);
            };

            scope.removeFilter = function(filter) {
              removeFacetElement=[];
              removeFacetElement.push(filter.key);
              var keys = Object.keys(filter.value);
              if (angular.isObject(filter.value) && keys[0] != 0) {
                removeFacetElement.push(keys[0])
                ngSearchFormCtrl.updateState(removeFacetElement, filter.value[keys[0]]);
              } else {
                removeFacetElement.push(filter.value)
                ngSearchFormCtrl.updateState(removeFacetElement, true);
              }
            };

            scope.removeAll = function() {
              removeAllFilters();
            };
          }
        };

      }]
  );

  module.filter('translatearray', ['$translate', function($translate) {

    var filterFunc = function(input, separator) {
      var result;
      if (angular.isString(input) && input.startsWith("+dateStamp:")) {
        result = input.split(/\[(.*?)\]/)[1]
      }
      else if (angular.isString(input)){
        result = input;
      }
      else {
        angular.forEach(input, function(value, key) {
          result = key
        })
      }
      return result;
    };

    return filterFunc;
  }]);

})();
