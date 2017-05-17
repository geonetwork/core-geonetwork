/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
          params: '=',
          currentFacets: '=',
          noCollapse: '@'
        },
        link: function(scope, element, attrs, controller) {

          var initialMaxItems = 5;

          // Facet is collapsed if not in current search criteria
          function isFacetsCollapse(facetKey) {
            if (scope.noCollapse) {
              return false;
            } else {
              return !(scope.params &&
                  angular.isDefined(scope.params[facetKey]));
            }
          };

          scope.collapsed = isFacetsCollapse(scope.indexKey);
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
          params: '=',
          currentFacets: '='
        },
        link: function(scope) {
          scope.facetConfig = [];

          // Facet is collapsed if not in current search criteria
          scope.isFacetsCollapse = function(facetKey) {
            return !angular.isDefined(scope.params[facetKey]);
          };

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
    '$q',
    '$filter',
    'gnFacetService',
    'gnFacetConfigService',
    'gnHttp',
    function($q, $filter, gnFacetService, gnFacetConfigService, gnHttp) {

      var updateLabelFromInfo = function(facets, groups, lang) {
        angular.forEach(facets, function(f) {
          for (var i = 0; i < groups.length; i++) {
            var o = groups[i];
            if (o.name == f['@name']) {
              f['@label'] = o.label[lang];
            }
            f['name'] = f['@label'] || f['@name'];
          }
        });
      };

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/facets/' +
            'partials/facet-multiselect.html',
        scope: true,
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, element, attrs, controller) {

              var delimiter = ' or ';
              var oldParams;
              var groups;
              scope.title = attrs['gnFacetMultiselectTitle'];

              scope.name = attrs.gnFacetMultiselect;
              scope.contentCollapsed =
                  attrs.gnFacetMultiselectCollapsed == 'true';

              gnFacetConfigService.loadConfig('hits').

                  // Load facets global config from cache
                  then(function(data) {
                    if (angular.isArray(data)) {
                      for (var i = 0; i < data.length; i++) {
                        if (data[i].name == scope.name) {
                          scope.facetConfig = data[i];
                          break;
                        }
                      }
                    }
                  }).then(function() {
                    var promises = [];

                    // Load groups label for 'publishedForGroup'
                    if (scope.facetConfig.label == 'publishedForGroup') {
                      promises.push(gnHttp.callService('info', {
                        type: 'groupsAll'}).
                          success(function(data) {
                            groups = data.group;
                          }));
                    }

                    // When everything is loaded, watch the summary response
                    // to update the multi facet list
                    $q.all(promises).then(function() {
                      scope.$watch('searchResults.facet', function(v) {
                        if (v && scope.facetConfig && scope.facetConfig.label) {
                          var facets = v[scope.facetConfig.label];

                          if (scope.facetConfig.label == 'publishedForGroup') {
                            updateLabelFromInfo(facets, groups, scope.lang);
                            facets = $filter('orderBy')(facets, 'name');
                            facets = $filter('filter')(facets, function(i) {
                              return i.name != 'INTERNET';
                            });

                          }
                          scope.facetObj = facets;
                        }
                      });
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
                    scope.searchObj.params[scope.facetConfig.key]
                    .split(delimiter)
                    .indexOf(value) >= 0;
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
        }
      };
    }]);

  module.directive('gnFacetGraph', ['$timeout', function($timeout) {

    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/search/facets/' +
          'partials/facet-graph.html',
      scope: {
        field: '=',
        callback: '='
      },
      link: function(scope, element, attrs, controller) {
        if (!scope.field) { return; }

        var tm = new TimeLine(element.find('.ui-timeline')[0],
            scope.field, scope.callback);

        // dates must be sorted ASC
        scope.$watch('field.datesCount', function(counts) {
          if (counts) {
            var data = counts.map(function(d) {
              return {
                event: d.value,
                time: {
                  begin: d.value,
                  end: d.value
                },
                value: d.count
              };
            });

            // apply data to graph
            tm.setTimeline(data);
          }
        });

        // call graph resize when it is expanded
        scope.$watch('field.expanded', function(exp) {
          if (exp) {
            setTimeout(function() {
              tm.recomputeSize();
            });
          }
        });
      }
    };

  }]);

})();
