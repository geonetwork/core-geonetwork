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
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/search/facets/' +
            'partials/facet-item.html';
        },
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
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/search/facets/' +
            'partials/facet-list.html';
        },
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
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/search/facets/' +
            'partials/facet-breadcrumb.html';
        },
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
    '$http',
    '$filter',
    'gnFacetService',
    'gnFacetConfigService',
    'gnHttp',
    'gnSearchSettings',
    function($q, $http, $filter, gnFacetService, gnFacetConfigService, gnHttp,
             gnSearchSettings) {

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
        templateUrl: function(elem, attrs) {
          return attrs.template || '../../catalog/components/search/facets/' +
            'partials/facet-multiselect.html';
        },
        scope: true,
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, element, attrs, controller) {

              var groups;
              scope.title = attrs['gnFacetMultiselectTitle'];
              scope.hasFilter = attrs['gnFacetMultiselectFilter'] == 'true';

              scope.name = attrs.gnFacetMultiselect;
              scope.contentCollapsed =
                  attrs.gnFacetMultiselectCollapsed == 'true';

              scope.ctrl = {};

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
                      promises.push($http.get('../api/groups').
                          then(function(r) {
                            groups = r.data;
                          }, function(r) {
                            console.log(r);
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
                              // Specific Sextant
                              if (gnSearchSettings.configWhat) {
                                var catalog =
                                    gnSearchSettings.configWhat.split(',');
                                return catalog.indexOf(i['@name']) >= 0;
                              }
                              // end specific
                              return i.name != 'INTERNET';
                            });
                          }
                          // Specific Sextant
                          else if (scope.facetConfig.key == 'orgName' &&
                              gnSearchSettings.configWho) {
                            facets = $filter('filter')(facets, function(i) {
                              var catalog =
                                  gnSearchSettings.configWho.split(',');
                              return catalog.indexOf(i['@name']) >= 0;
                            });
                          }
                          // end specific

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
                var p = scope.searchObj.params[scope.facetConfig.key];
                if (angular.isString(p)) {
                  p = [p];
                }
                return p && p.indexOf(value) >= 0;
                //return scope.searchObj.params[scope.facetConfig.key] &&
                //    scope.searchObj.params[scope.facetConfig.key]
                //    .split(delimiter)
                //    .indexOf(value) >= 0;
              };

              //TODO improve performance here, maybe to complex $watchers
              // add subdirective to watch a boolean and make only one
              // watcher on searchObj.params
              scope.updateSearch = function(value, e) {
                var key = scope.facetConfig.key;
                var search = scope.searchObj.params[key];

                // null, undefined or ''
                if (!search) {
                  scope.searchObj.params[key] = [value];
                }
                else {
                  if (angular.isArray(search)) {
                    var idx = search.indexOf(value);
                    if (idx < 0) {
                      search.push(value);
                    }
                    else {
                      search.splice(idx, 1);
                    }
                  }
                  else {
                    if(search == value) {
                      scope.searchObj.params[key] = undefined;
                    }
                    else {
                      scope.searchObj.params[key] = [search].concat([value]);
                    }
                  }
                }
                scope.$emit('resetSearch', scope.searchObj.params);
                e.preventDefault();
              };

              /**
               * Filter facet inputs depending on text filter
               * @param {Object} v Facet item.
               */
              scope.filterInputs = function(v) {
                var filter = scope.ctrl.activeFilter;
                if (!filter) return true;
                filter = filter.toLowerCase();
                return v.name.toLowerCase().indexOf(filter) >= 0;

              };

              scope.$on('beforeSearchReset', function() {
                scope.ctrl.activeFilter = '';
              });
            }
          };
        }
      };
    }]);


  /**
   * @ngdoc directive
   * @name gn_facets_directive.directive:gnFacetDaterange
   * @function
   *
   * @description
   * Shows a double datepicker input to select a range among available dates.
   * Can display the available dates as a graph using Y for occurence count.
   * If dates available, the datepickers will initialize at min/max date.
   */
  module.directive('gnFacetDaterange', ['$timeout', function($timeout) {
    return {
      restrict: 'A',
      replace: true,
      templateUrl: function(elem, attrs) {
        return attrs.template || '../../catalog/components/search/facets/' +
          'partials/facet-daterange.html';
      },
      scope: {
        values: '<gnFacetDaterange',  // object with 'from' and 'to' properties as DD-MM-YYYY
        dates: '<availableDates',   // array of avalable dates as epoch (sorted asc)
        datesCount: '<datesCount',  // an object with keys as epoch date & values as counts (sorted asc)
        updateCallback: '&callback',  // called when values are updated: arguments are 'from' and 'to' as DD-MM-YYYY
        expanded: '<graphExpanded'  // true means the graph size will be recomputed (first opening)
      },
      link: function(scope, element, attrs, controller) {
        // save initial min/max dates
        if (scope.dates) {
          scope.minDate = moment(scope.dates[0]).format('DD-MM-YYYY');
          scope.maxDate = moment(scope.dates[scope.dates.length - 1])
            .format('DD-MM-YYYY');
        }

        // this object will be used for the datepickers
        scope.pickerValues = angular.extend({}, scope.values);

        // shortcut for graph update callback
        scope.graphCallback = function(dateFrom, dateTo) {
          scope.pickerValues.from = dateFrom;
          scope.pickerValues.to = dateTo;
          scope.updateCallback({
            from: dateFrom,
            to: dateTo
          });
        };

        // graph handling (this is optional)
        // the graph requires the datesCount object, otherwise it has nothing
        // to display!
        scope.showGraph = scope.$eval(attrs.showGraph);
        if (scope.$eval(attrs.showGraph)) {
          scope.graph = new TimeLine(element.find('.ui-timeline')[0],
            scope.graphCallback, {
              showAsHistogram: true
            });

          // this updates the graph view to be in sync with the current values
          // if no value available the graph will show the entire range of dates
          var refreshGraphRange = function() {
            if (!scope.values) {
              scope.graph.setDateRange(null, null);
              return;
            }
            scope.graph.setDateRange(scope.values.from || null,
              scope.values.to || null);
          };

          // dates must be sorted ASC
          scope.$watch('datesCount', function(counts) {
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
              scope.graph.setTimeline(data);
              refreshGraphRange();
            }
          });

          // call graph resize when it is expanded
          var expandedWatch = scope.$watch('expanded', function(exp) {
            if (exp) {
              setTimeout(function() {
                scope.graph.recomputeSize();
                refreshGraphRange();
                sizeComputed = true;
                expandedWatch();    // unregister the watcher
              });
            }
          });
        }

        // update view if dates are changed from outside
        scope.$watch(function() {
          if (!scope.values) {
            return '';
          }
          return scope.values.from + ' ' + scope.values.to;
        }, function() {
          // refresh graph if necessary
          if (scope.graph && scope.graph.initialized) {
            refreshGraphRange();
          }

          // refresh datepicker (use initial min/max as fallback)
          scope.pickerValues.from = (scope.values && scope.values.from)
            || scope.minDate || '';
          scope.pickerValues.to = (scope.values && scope.values.to)
            || scope.maxDate || '';
        });
      }
    };

  }]);

})();
