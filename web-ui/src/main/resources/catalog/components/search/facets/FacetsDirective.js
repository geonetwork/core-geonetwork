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

(function () {
  goog.provide("gn_facets_directive");

  var module = angular.module("gn_facets_directive", []);

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
  module.directive("gnFacetDaterange", [
    "$timeout",
    function ($timeout) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/search/facets/" + "partials/facet-daterange.html"
          );
        },
        scope: {
          values: "<gnFacetDaterange",
          // object with 'from' and 'to' properties as DD-MM-YYYY
          dates: "<availableDates",
          // array of available dates as epoch (sorted asc)
          datesCount: "<datesCount",
          // array of counts per date; each element holds a value (date epoch) and a count property
          updateCallback: "&callback",
          // called when values are updated:
          // arguments are 'from' and 'to' as DD-MM-YYYY
          expanded: "<graphExpanded"
          // true means the graph size will be recomputed (first opening)
        },
        link: function (scope, element, attrs, controller) {
          // save initial min/max dates
          if (scope.dates) {
            scope.minDate = moment(scope.dates[0]).format("DD-MM-YYYY");
            scope.maxDate = moment(scope.dates[scope.dates.length - 1]).format(
              "DD-MM-YYYY"
            );
          }

          // this object will be used for the datepickers
          scope.pickerValues = angular.extend({}, scope.values);

          // shortcut for graph update callback
          scope.graphCallback = function (dateFrom, dateTo) {
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
            scope.graph = new TimeLine(
              element.find(".ui-timeline")[0],
              scope.graphCallback,
              {
                showAsHistogram: true
              }
            );

            // this updates the graph view to be
            // in sync with the current values
            // if no value available the graph
            // will show the entire range of dates
            var refreshGraphRange = function () {
              if (!scope.values) {
                scope.graph.setDateRange(null, null);
                return;
              }
              scope.graph.setDateRange(
                scope.values.from || null,
                scope.values.to || null
              );
            };

            // dates must be sorted ASC
            scope.$watch("datesCount", function (counts) {
              if (counts) {
                var data = counts.map(function (d) {
                  return {
                    begin: d.begin ? d.begin : d.value,
                    end: d.end ? d.end : d.value,
                    value: d.count
                  };
                });

                // apply data to graph
                scope.graph.setTimeline(data);
                refreshGraphRange();
              }
            });

            // call graph resize when it is expanded
            var expandedWatch = scope.$watch("expanded", function (exp) {
              if (exp) {
                setTimeout(function () {
                  scope.graph.recomputeSize();
                  refreshGraphRange();
                  sizeComputed = true;
                  expandedWatch(); // unregister the watcher
                });
              }
            });
          }

          // update view if dates are changed from outside
          scope.$watch(
            function () {
              if (!scope.values) {
                return "";
              }
              return scope.values.from + " " + scope.values.to;
            },
            function () {
              // refresh graph if necessary
              if (scope.graph && scope.graph.initialized) {
                refreshGraphRange();
              }

              // refresh datepicker (use initial min/max as fallback)
              scope.pickerValues.from =
                (scope.values && scope.values.from) || scope.minDate || "";
              scope.pickerValues.to =
                (scope.values && scope.values.to) || scope.maxDate || "";
            }
          );
        }
      };
    }
  ]);
})();
