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
  goog.provide("gn_search_form_results_directive");

  var module = angular.module("gn_search_form_results_directive", []);

  module.directive("gnSearchFormResults", [
    "gnSearchManagerService",
    function (gnSearchManagerService) {
      var activeClass = "active";

      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/search/searchmanager/partials/" +
          "searchresults.html",
        scope: {
          searchResults: "=",
          paginationInfo: "=paginationInfo",
          selection: "=selectRecords",
          selectionBucket: "@",
          onMdClick: "="
        },
        link: function (scope, element, attrs) {
          if (angular.isUndefined(scope.selectionBucket)) {
            scope.selectionBucket = (Math.random() + "").replace(".", "");
          }

          // get init options
          scope.options = {};
          angular.extend(scope.options, {
            mode: attrs.gnSearchFormResultsMode,
            selection: {
              mode: attrs.gnSearchFormResultsSelectionMode
            }
          });

          /**
           * Triggered on a metadata row click.
           * Call the function given in directive parameter on-md-click.
           * If this function is not defined, then call the select method
           * if the directive has a selection model.
           */
          scope.onClick = function (md) {
            if (angular.isFunction(scope.onMdClick)) {
              scope.onMdClick(md);
            } else if (angular.isFunction(scope.select)) {
              scope.select(md);
            }
          };

          // Manage selection
          if (scope.options.selection.mode) {
            scope.selection = [];
            if (scope.options.selection.mode.indexOf("local") >= 0) {
              /**
               * Define local select function
               * Manage an array scope.selection containing
               * all selected MD
               */
              scope.select = function (md) {
                if (scope.options.selection.mode.indexOf("multiple") >= 0) {
                  if (scope.selection.indexOf(md) < 0) {
                    scope.selection.push(md);
                  } else {
                    scope.selection.splice(scope.selection.indexOf(md), 1);
                  }
                } else {
                  // Unselect current
                  if (scope.selection.length === 1 && scope.selection[0]._id === md._id) {
                    scope.selection.pop();
                  } else {
                    scope.selection.pop();
                    scope.selection.push(md);
                  }
                }
              };
            } else {
              scope.select = function (md) {
                if (scope.options.selection.mode.indexOf("multiple") >= 0) {
                  if (md.selected === false) {
                    md.selected = true;
                    gnSearchManagerService
                      .select(md.uuid, scope.selectionBucket)
                      .then(updateSelectionNumber);
                  } else {
                    md.selected = false;
                    gnSearchManagerService
                      .unselect(md.uuid, scope.selectionBucket)
                      .then(updateSelectionNumber);
                  }
                } else {
                  // TODO: clear selection ?
                  console.log("Single selection is not " + "supported in remote mode.");
                  //  md.selected = true;
                  //  gnSearchManagerService.select(md.uuid)
                  //  .then(updateSelectionNumber);
                }
              };
            }
          }

          var updateSelectionNumber = function (data) {
            scope.selection = {
              length: data[0]
            };
          };

          scope.selectAll = function (all) {
            angular.forEach(scope.searchResults.records, function (md) {
              md.selected = all;
            });
            if (all) {
              gnSearchManagerService
                .selectAll(scope.selectionBucket)
                .then(updateSelectionNumber);
            } else {
              gnSearchManagerService
                .selectNone(scope.selectionBucket)
                .then(updateSelectionNumber);
            }
          };

          /**
           * If local, selection is handled in an array on the client
           * if not, selection is handled on server side and
           * search results contains information if a record is selected or not.
           */
          scope.isSelected = function (md) {
            if (!scope.options.selection || !scope.options.selection.mode) {
              return false;
            }
            var targetUuid = md.uuid;
            var selected = false;
            if (scope.options.selection.mode.indexOf("local") >= 0) {
              angular.forEach(scope.selection, function (md) {
                if (md.uuid === targetUuid) {
                  selected = true;
                }
              });
            } else {
              selected = md.selected;
            }
            return selected;
          };

          scope.$on("resetSelection", function (evt) {
            if (scope.selection) {
              scope.selection = [];
            }
          });

          // Default settings for pagination
          // TODO: put parameters in directive
          if (scope.paginationInfo === null) {
            scope.paginationInfo = {
              pages: -1,
              currentPage: 1,
              hitsPerPage: 5
            };
          }
        }
      };
    }
  ]);
})();
