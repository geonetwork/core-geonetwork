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
                var queryParts = raw.split('/');
                var dimensionKey = queryParts[0];

                var dimension;
                scope.dimensions.forEach(function (d) {
                  if (d['@name'] === dimensionKey) {
                    dimension = d;
                  }
                });

                if (!dimension) {
                  return
                }

                var categoryLabel, rootPath = decodeURIComponent(queryParts[1]);
                function lookupCategory(categories, currentQueryPartIndex) {
                  categories.forEach(function (c) {
                    if (c['@value'] === decodeURIComponent(queryParts[currentQueryPartIndex])) {
                      currentQueryPartIndex++;
                      if (currentQueryPartIndex === queryParts.length) {
                        categoryLabel = c['@label'];
                        return true;
                      } else if(c.category) {
                        lookupCategory(c.category, currentQueryPartIndex);
                      }
                    }
                  })
                }
                if (dimension.category) {
                  lookupCategory(dimension.category, 1);
                }

                return {
                  key: dimension['@label'],
                  value: categoryLabel || rootPath,
                  isFacet: true,
                  facetKey: dimension['@name']
                };
              });
            }

            function removeFacet(facetKey, facetQuery) {
              if (!facetQuery) {
                return;
              }
              var facets = facetQuery.split('&').filter(function (facet) {
                return facet.split('/')[0] !== facetKey;
              });
              return facets.join('&');
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
                    value: 'boundingBox'
                  });
                  continue;
                }

                // special logic for categories: add cat- prefix to each category for translations
                if (filterKey === '_cat') {
                  scope.currentFilters.push({
                    key: filterKey,
                    value: value.split(' or ').map(function(val) {
                      return 'cat-' + val;
                    }).join(' or ')
                  });
                  continue;
                }

                // general case
                scope.currentFilters.push({
                  key: filterKey,
                  value: value
                });
              }
            }, true);

            scope.removeFilter = function(filter) {
              if (filter.isFacet) {
                var facetQuery = removeFacet(filter.facetKey, getSearchParams()['facet.q']);
                setSearchParameter('facet.q', facetQuery);
              } else {
                setSearchParameter(filter.key, null);
              }
            };

            scope.removeAll = function() {
              removeAllFilters();
            }
          }
        };

      }]
  );

  module.filter('translatearray', ['$translate', function($translate) {

    var filterFunc = function(input, separator) {
      var sep = separator;
      if (!separator) {
        sep = ' or ';
      }

      if (!input || !angular.isString(input)) {
        return input;
      }

      var tokens = input.split(sep);

      var result = '';
      angular.forEach(tokens, function(token, index) {
        result += $translate.instant(token.trim());
        if (index != tokens.length - 1) {
          var sepTranslated = $translate.instant(sep.trim());
          result += ' ' + sepTranslated + ' ';
        }
      });

      return result;
    };

    return filterFunc;
  }]);

})();
