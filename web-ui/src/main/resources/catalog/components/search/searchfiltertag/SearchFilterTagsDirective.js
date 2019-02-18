(function() {
  goog.provide('search_filter_tags_directive');

  goog.require('gn_thesaurus_service');

  var module = angular.module('search_filter_tags_directive',
      ['gn_thesaurus_service', 'pascalprecht.translate']);
  module.filter('decodeURIComponent', function() {
    return window.decodeURIComponent;
  });

  module.directive('searchFilterTags',
      ['$location', 'gnThesaurusService', '$q', '$cacheFactory', '$browser', '$timeout',
       function($location, gnThesaurusService, $q, $cacheFactory, $browser, $timeout) {
         var cache = $cacheFactory('locationsSearchFilterTags');
         var useLocationParameters = true;
         var thesaurusKey = 'external.place.regions';


         var searchInFilters = function(filters, filterKey, filterType) {
           var found = false;
           for (var i = 0; i < filters.length && !found; i++) {
             var filter = filters[i];
             found = filter.key === filterKey && filter.type === filterType;
           }

           return found;
         };

         var generateCurrentFilters =
          function(filters, currentFilters, controller) {
            // Remove old filters not present in filters keeping the old order
            var deferred = $q.defer();

            var newFilters = [];
            var newFacets = getFacets(filters['facet.q']);
            var newLocationsPromise = getLocationsPromise(filters['geometry']);
            newLocationsPromise.then(function(locations) {

              angular.forEach(currentFilters, function(oldF, index) {
                if (newFacets.length > 0) {
                  if (oldF.type == 'facet' &&
                 searchInFacets(oldF, newFacets).length > 0 &&
                 searchInFacets(oldF, newFacets)[0].value === oldF.value) {
                    var label = getLabelForFacet(
                   oldF.key, oldF.value, controller.getSearchResults());

                    newFilters = newFilters.concat({
                      type: 'facet',
                      key: oldF.key,
                      value: oldF.value,
                      labelKey: label
                    });
                  } else if (oldF.type === 'geometry' &&
                 searchInGeometries(oldF, locations) > 0) {
                    newFilters = newFilters.concat({
                      type: 'geometry',
                      key: oldF.key,
                      value: oldF.value
                    });
                  } else if (oldF.type === 'boolean' &&
                 oldF.value === true && filters[oldF.key] !== undefined) {
                    newFilters = newFilters.concat({
                      type: 'boolean',
                      key: oldF.key,
                      value: true,
                      labelKey: getLabelKey(oldF.key)
                    });
                  } else if (oldF.value === filters[oldF.key]) {
                    newFilters = newFilters.concat(oldF);
                  }
                }
              });


              // Add new filters
              for (var filterKey in filters) {
                if (filterKeyIsTaggable(filterKey) &&
               !(searchInFilters(newFilters, filterKey,
               getTypeForKey(filterKey))) &&
               !hasEmptyValue(filters[filterKey])) {
                  var newFilter =
                 createNewFilter(filterKey, filters[filterKey]);
                  if (newFilter.length > 0) {
                    newFilters = newFilters.concat(newFilter);
                  }
                }
                if (filterKey === 'facet.q') {
                  angular.forEach(newFacets, function(nf, index) {
                    if (!searchInFilters(newFilters, nf.dimension, 'facet')) {
                      newFilters = newFilters.concat(
                     createNewFilterFromFacets(nf, controller));
                    }
                  });
                }
                if (filterKey === 'geometry') {
                  angular.forEach(locations, function(newLocation) {
                    if (!searchInFilters(newFilters,
                        newLocation.key, 'geometry')) {
                      newFilters = newFilters.concat(newLocation);
                    }
                  });
                }
              }
              deferred.resolve(newFilters);
            });

            return deferred.promise;

          };

         var hasEmptyValue = function(value) {
           if (!angular.isDefined(value) || value === null ||
            (angular.isString(value) && value.trim() === '')) {
             return true;
           } else {
             return false;
           }
         };

         var getLabelForFacet = function(key, value, searchResults) {
           if (!searchResults || !searchResults.dimension) {
             return value;
           }

           var currentDimension =
            $.grep(searchResults.dimension, function(elem, index) {
              return elem['@name'] === key;
            });
           if (currentDimension.length > 0) {
             var d = currentDimension[0];
             // Find label in dimension categories
             if (d.category) {
               var category = $.grep(d.category, function(cat, index) {
                 return cat['@value'] === decodeURIComponent(value);
               });
               if (category.length > 0) {
                 return category[0]['@label'];
               }
             }
           }

           return value;

         };

         /**
         *
         * @param {string} key the key of the param
         * @param {string} filterParam the value of the filter
         * @return {array}
         */
         var createNewFilter = function(key, filterParam) {
           var result = [];
           if (getLabelKey(key)) {
             var newFilter = {};
             newFilter['type'] = 'boolean';
             newFilter['value'] = filterParam;
             newFilter['labelKey'] = getLabelKey(key);
             newFilter['key'] = key;
             result.push(newFilter);
           } else if (key === 'geometry') {
            /*var newFilter = {};
             newFilter['type'] = 'geometry';
             newFilter['value'] = filterParam;
             newFilter['key'] = key;
             newFilter['labelKey'] = 'Localisation';
             result.push(newFilter);*/
           } else {
             var newFilter = {};
             newFilter['key'] = key;
             newFilter['type'] = 'standard';

             var val = filterParam;
             if (angular.isArray(val)) {
               val = val[0];
             } else if (angular.isObject(val) && val.label && val.label != '') {
               val = val.label;
             }
             newFilter['value'] = angular.isString(val) ? val : '';
             result.push(newFilter);
           }

           return result;
         };

         var getTypeForKey = function(key) {
           var type = null;
           if (getLabelKey(key)) {
             type = 'boolean';
           } else if (key === 'geometry') {
             type = 'geometry';
           } else {
             type = 'standard';
           }
           return type;
         };

         var createNewFilterFromFacets = function(facet, controller) {

           var newFilter = {};
           var result = [];
           var label = getLabelForFacet(
            facet.dimension, facet.value, controller.getSearchResults());
           newFilter['type'] = 'facet';
           newFilter['value'] = facet.value;
           newFilter['key'] = facet.dimension;
           newFilter['labelKey'] = label;
           result.push(newFilter);
           return result;

         };

         var getLabelKey = function(labelKey) {
           var availableLabels = {
             dynamic: 'Visualisation',
             download: 'DownloadData',
             nodynamicdownload: 'DataOnRequest'
           };

           return availableLabels[labelKey];

         };

         var filterKeyIsTaggable = function(filterKey) {
           var filtersNotTaggables = [
             'bboxes', 'fast', 'facet.q', 'from', 'geometry',
             'resultType', 'sortBy', 'sortOrder', '_owner', '_isTemplate',
             'to', '_content_type', 'ownRecords', 'bucket'];
           return $.inArray(filterKey, filtersNotTaggables) == -1;
         };

         var searchInFacets = function(filter, newFacets) {
           var key = filter.key;
           return $.grep(newFacets, function(elem, indexInArray) {
             return elem && elem.dimension && elem.dimension == key;
           }).length != 0;
         };

         var searchInGeometries = function(filter, newLocations) {
           var key = filter.key;
           return $.grep(newLocations, function(elem, indexInArray) {
             return elem && elem.key && elem.key == key;
           }).length != 0;
         };


         var getFacets = function(facetQParam) {
           var categoryList = [];
           if (!facetQParam) {
             return categoryList;
           }
           var dimensionList =
            facetQParam.split('&');
           $.each(dimensionList, function(idx) {
             // Dimension filter contains the dimension name first
             // and then the drilldown path. User may uncheck
             // an element in the middle of the path. In such case
             // only activate the parent node.
             var dimensionFilter = dimensionList[idx].split('/');

             // Dimension but not in that category path. Add filter.
             if (dimensionFilter.length == 2) {
               categoryList.push({
                 dimension: dimensionFilter[0],
                 value: dimensionFilter[1]
               });
             }
           });

           return categoryList;
         };

         var getLocationsPromise = function(geometryParam) {
           var prefix = 'region:';
           var locations = [];
           if (!geometryParam || geometryParam.indexOf(prefix) !== 0 ||
            geometryParam === prefix) {
             return $q.all();
           }
           var locationList =
            geometryParam.substring(prefix.length).split(/\s*,\s*/);
           var promises = [];
           $.each(locationList, function(idx) {
             promises.push(
             getKeywordFromUri(locationList[idx]).then(function(kw) {
               locations.push(
                {
                  value: kw.label,
                  key: kw.props.uri,
                  type: 'geometry'
                }
               );
             }));
           });
           var deferred = $q.defer();
           $q.all(promises).then(function() {
             deferred.resolve(locations);
           });
           return deferred.promise;
         };

         var getKeywordFromUri = function(uri) {
           var defer = $q.defer();
           if (!cache.get(uri)) {
             gnThesaurusService.lookupURI(
              thesaurusKey, uri).then(
             function(keyword) {
               if (keyword) {
                 var kw = {};
                 kw['label'] =
                 keyword.prefLabel[Object.keys(keyword.prefLabel)[0]];
                 kw['props'] = {};
                 kw['props']['uri'] = keyword.uri;
                 cache.put(uri, kw);
                 defer.resolve(kw);
               } else {
                 // defer.reject(keyword);
               }

             }, function(rejected) {
               defer.reject(rejected);
             });
           } else {
             $browser.defer(function() {
               defer.resolve(cache.get(uri));
             });
           }
           return defer.promise;
         };

         var buildQParam = function(facetsArray) {
           // Build facet.q
           var facetQParam = '';
           $.each(facetsArray, function(idx, facet) {
             if (facet.value) {
               facetQParam = facetQParam +
                facet.key +
                '/' +
                facet.value +
                (idx < facetsArray.length - 1 ? '&' : '');
             }
           });
           return facetQParam;
         };

         var buildGeometryParam = function(locationsArray) {
           var geometryParam = 'region:';
           var locations = [];
           if (locationsArray && locationsArray.length > 0) {
             $.each(locationsArray, function(idx, location) {
               locations.push(location.key);
             });
           } else {
             return null;
           }
           return geometryParam + locations.join(',');
         };

         var getSearchParameters =
          function(useLocation, locationProvider, controller) {
            if (useLocation) {
              return locationProvider.search();
            } else {
              return controller.getFinalParams();
            }
          };

         var setSearchParameter =
          function(useLocation, locationProvider,
                   controller, paramKey, paramValue) {
            if (useLocation) {
              locationProvider.search(paramKey, paramValue);
            } else {
              var params = controller.getSearchParams();
              if (paramValue == null) {
                delete params[paramKey];
              } else {
                params[paramKey] = paramValue;
              }

            }
          };

         return {
           restrict: 'EA',
           require: '^ngSearchForm',
           templateUrl: '../../catalog/components/' +
           'search/searchfiltertag/partials/' +
           'searchFilterTagsTemplate.html',
           scope: {
             privateVar: '@',
             useLocationParameters: '@',
             thesaurusKey: '@'
           },
           link: function(scope, element, attrs, ngSearchFormCtrl) {
             if (scope.useLocationParameters === 'false') {
               useLocationParameters = false;
             }

             if (scope.thesaurusKey && scope.thesaurusKey !== '') {
               thesaurusKey = scope.thesaurusKey;
             }

             scope.currentFilters = [];
             scope.$watch(function() {
               return getSearchParameters(useLocationParameters,
                $location, ngSearchFormCtrl);
             }, function(newFilters, oldVal, currentScope) {
               $timeout(function () {
                 generateCurrentFilters(newFilters,
                   currentScope.currentFilters, ngSearchFormCtrl)
                   .then(function (calculatedFilters) {
                     scope.currentFilters = calculatedFilters;
                   });
               }, 100);
             }, true);

             scope.removeFilter = function(filter) {
               if (filter.type === 'facet') {
                 var newFacets = $.grep(scope.currentFilters,
                  function(element, index) {
                    return element.type === 'facet' &&
                   element.key !== filter.key;
                  });
                 var newFacetQParam = buildQParam(newFacets);
                 if (newFacetQParam == '') {
                   newFacetQParam = null;
                 }
                 setSearchParameter(useLocationParameters,
                  $location, ngSearchFormCtrl, 'facet.q', newFacetQParam);
                // $location.search('facet.q', newFacetQParam);

               } else if (filter.type === 'geometry') {
                 var newLocations = $.grep(scope.currentFilters,
                  function(element, index) {
                    return element.type === 'geometry' &&
                   element.key !== filter.key;
                  }
                 );
                 var newGeometryParam = buildGeometryParam(newLocations);
                 setSearchParameter(useLocationParameters,
                  $location, ngSearchFormCtrl, 'geometry', newGeometryParam);
                // $location.search('geometry', newGeometryParam);
               } else if (filter.type === 'boolean') {
                 setSearchParameter(useLocationParameters,
                  $location, ngSearchFormCtrl, filter.key, null);
                // $location.search(filter.key, null);
               } else {
                 setSearchParameter(useLocationParameters,
                  $location, ngSearchFormCtrl, filter.key, null);
                 //$location.search(filter.key, null);
               }
               if (!useLocationParameters) {
                 // Programmatically start the new search.
                 ngSearchFormCtrl.triggerSearch();
               }

             };
           }
         };
       }]);

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
