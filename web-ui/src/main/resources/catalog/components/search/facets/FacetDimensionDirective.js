(function() {
  goog.provide('gn_facets_dimension_directive');
  goog.require('gn_utility_service');

  var module = angular.module('gn_facets_dimension_directive',
      ['gn_utility_service']);

  module.directive('gnFacetDimensionList', [
    'gnFacetConfigService', '$timeout',
    function(gnFacetConfigService, $timeout) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/dimension-facet-list.html',
        scope: {
          dimension: '=gnFacetDimensionList',
          facetType: '=',
          params: '='
        },
        link: function(scope, element) {
          scope.facetQuery = scope.params['facet.q'];
          scope.facetConfig = null;

          scope.collapseAll = function() {
            $timeout(function() {
              element.parent().find('div.gn-facet > h4').click();
            });
          };

          // Facet is collapsed if not in current search criteria
          scope.isFacetsCollapse = function(facetKey) {
            return !angular.isDefined(scope.params[facetKey]);
          };

          // Load facet configuration to know which index field
          // correspond to which dimension.
          gnFacetConfigService.loadConfig(scope.facetType).
              then(function(data) {
                scope.facetConfig = {
                  config: data,
                  map:  {}
                };

                angular.forEach(scope.facetConfig.config, function(key) {
                  scope.facetConfig.map[key.label] = key.name;
                });
              });
        }
      };
    }]);

  module.directive('gnFacetDimensionCategory', [
    'gnFacetConfigService', 'RecursionHelper', '$parse',
    function(gnFacetConfigService, RecursionHelper, $parse) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/dimension-facet-category.html',
        scope: {
          category: '=gnFacetDimensionCategory',
          categoryKey: '=',
          path: '=',
          params: '=',
          facetConfig: '='
        },
        compile: function(element) {
          // Use the compile function from the RecursionHelper,
          // And return the linking function(s) which it returns
          return RecursionHelper.compile(element,
              function(scope, element, attrs) {
                var initialMaxItems = 5;
                scope.initialMaxItems = initialMaxItems;
                scope.maxItems = initialMaxItems;
                scope.toggleAll = function() {
                  scope.maxItems = (scope.maxItems == Infinity) ?
                      initialMaxItems : Infinity;
                };

                // Facet drill down is based on facet.q parameter.
                // The facet.q parameter contains a list of comma separated
                // dimensions
                // <dimension_name>{"/"<category_value>}
                // Note that drill down paths use '/' as the separator
                // between categories in the path, so embedded '/' characters
                // in categories should be escaped using %2F or alternatively,
                // each category in the path url encoded in addition to
                // normal parameter encoding.
                //
                // Multiple drill down queries can be specified by providing
                // multiple facet.q parameters or by combining drill down
                // queries in one facet.q parameter using '&'
                // appropriately encoded.
                //
                // http://localhost:8080/geonetwork/srv/fre/q?
                // resultType=hierarchy&
                // facet.q=gemetKeyword/http%253A%252F%252Fwww.eionet.europa.eu
                //  %252Fgemet%252Fconcept%252F2643
                //  %2F
                //  http%253A%252F%252Fwww.eionet.europa.eu
                //    %252Fgemet%252Fconcept%252F2641

                /**
               * Build the drill down path based on current category value
               * and its parent.
               * @param {Object} category
               * @return {boolean|*}
               */
                scope.buildPath = function(category) {
                  category.path =
                      (scope.path === undefined ? '' : scope.path + '/') +
                      encodeURIComponent(category['@value']);
                  return category.path;
                };


                /**
               * Build the facet.q paramaeter
               */
                scope.filter = function(category, $event) {
                  if (!scope.facetConfig) {
                    return; // Facet configuration not yet loaded.
                  }

                  var checked = $event.currentTarget.checked;


                  // Extract facet.q info
                  if (angular.isUndefined(scope.params['facet.q'])) {
                    scope.params['facet.q'] = '';
                  }
                  var facetQParam = scope.params['facet.q'];
                  var dimensionList =
                      facetQParam.split('&');
                  var categoryList = [];
                  $.each(dimensionList, function(idx) {
                    // Dimension filter contains the dimension name first
                    // and then the drilldown path. User may uncheck
                    // an element in the middle of the path. In such case
                    // only activate the parent node.
                    var dimensionFilter = dimensionList[idx].split('/');

                    // Dimension but not in that category path. Add filter.
                    if (dimensionFilter[1] &&
                        dimensionFilter[0] !=
                        scope.facetConfig.map[scope.categoryKey]) {
                      categoryList.push({
                        dimension: dimensionFilter[0],
                        value: dimensionFilter.slice(1, dimensionFilter.length)
                      });
                    } else if (dimensionFilter[1] &&
                        dimensionFilter.length > 2 &&
                        dimensionFilter[0] ==
                        scope.facetConfig.map[scope.categoryKey]) {

                      var filteredElementInPath =
                          $.inArray(
                          encodeURIComponent(category['@value']),
                          dimensionFilter);
                      // Restrict the path to its parent
                      if (filteredElementInPath !== -1) {
                        categoryList.push({
                          dimension: scope.categoryKey,
                          value: dimensionFilter.
                              slice(1, filteredElementInPath).
                              join('/')
                        });
                      }
                    }
                  });
                  // Add or remove new category
                  if (checked) {
                    categoryList.push({
                      dimension: scope.categoryKey,
                      value: category.path
                    });
                  } else {

                  }

                  // Build facet.q
                  facetQParam = '';
                  $.each(categoryList, function(idx) {
                    if (categoryList[idx].value) {
                      facetQParam = facetQParam +
                          (scope.facetConfig.map[categoryList[idx].dimension] ||
                          categoryList[idx].dimension) +
                          '/' +
                          categoryList[idx].value +
                          (idx < categoryList.length - 1 ? '&' : '');
                    }
                  });
                  scope.params['facet.q'] = facetQParam;

                  scope.$emit('resetSearch', scope.params);
                  $event.preventDefault();
                };


                /**
               * Check that current category is already used
               * in current filter.
               * @param {Object} category
               * @return {boolean|*}
               */
                scope.isOnDrillDownPath = function(category) {
                  // Is selected if the category value is defined in the
                  // facet.q parameter (ie. combination of
                  // dim1/dim1val/dim1val2&dim2/dim2Val...).
                  category.isSelected =
                      angular.isUndefined(scope.params['facet.q']) ?
                      false :
                      ($.inArray(
                      encodeURIComponent(category['@value']),
                      scope.params['facet.q'].split(/&|\//)) !== -1 ||
                      $.inArray(
                      category['@value'],
                      scope.params['facet.q'].split(/&|\//)) !== -1);
                  return category.isSelected;
                };

                scope.isInFilter = function(category) {
                  if (!scope.facetConfig) {
                    return false;
                    // Facet configuration not yet loaded
                  }

                  var facetQParam = scope.params['facet.q'];
                  if (facetQParam === undefined) {
                    return false;
                  }
                  var dimensionList =
                      facetQParam.split('&');
                  var categoryList = [];
                  for (var i = 0; i < dimensionList.length; i++) {
                    var dimensionFilter = dimensionList[i].split('/');
                    if (dimensionFilter[0] ==
                        scope.facetConfig.map[scope.categoryKey] &&
                        ($.inArray(
                        encodeURIComponent(category['@value']),
                        scope.params['facet.q'].split(/&|\//)) !== -1 ||
                        $.inArray(
                        category['@value'],
                        scope.params['facet.q'].split(/&|\//)) !== -1)) {
                      return true;
                    }
                  }
                  return false;
                };

                scope.toggleNode = function(evt) {
                  el = evt ?
                      $(evt.currentTarget).parent() :
                      element.find('span.fa');
                  el.find('.fa').first()
                  .toggleClass('fa-minus-square')
                  .toggleClass('fa-plus-square');
                  el.children('div').toggleClass('hidden');
                  !evt || evt.preventDefault();
                  return false;
                };
              });
        }
      };
    }]);
})();
