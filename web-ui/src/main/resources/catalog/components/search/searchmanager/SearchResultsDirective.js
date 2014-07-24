(function() {
  goog.provide('gn_search_form_results_directive');

  var module = angular.module('gn_search_form_results_directive', []);

  module.directive('gnSearchFormResults', [
    'gnSearchManagerService',
    function(gnSearchManagerService) {

      var activeClass = 'active';

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'searchresults.html',
        scope: {
          searchResults: '=',
          paginationInfo: '=paginationInfo',
          selection: '=selectRecords',
          onMdClick: '='
        },
        link: function(scope, element, attrs) {

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
          scope.onClick = function(md) {
            if (angular.isFunction(scope.onMdClick)) {
              scope.onMdClick(md);
            } else if (angular.isFunction(scope.select)) {
              scope.select(md);
            }
          };

          // Manage selection
          if (scope.options.selection.mode) {
            scope.selection = [];
            if (scope.options.selection.mode.indexOf('local') >= 0) {

              /**
               * Define local select function
               * Manage an array scope.selection containing
               * all selected MD
               */
              scope.select = function(md) {
                if (scope.options.selection.mode.indexOf('multiple') >= 0) {
                  if (scope.selection.indexOf(md) < 0) {
                    scope.selection.push(md);
                  }
                  else {
                    scope.selection.splice(scope.selection.indexOf(md), 1);
                  }
                }
                else {
                  scope.selection.pop();
                  scope.selection.push(md);
                }
              };
            } else {
              scope.select = function(md) {
                if (scope.options.selection.mode.indexOf('multiple') >= 0) {
                  if (md['geonet:info'].selected === false) {
                    md['geonet:info'].selected = true;
                    gnSearchManagerService.select(md['geonet:info'].uuid)
                      .then(updateSelectionNumber);
                  } else {
                    md['geonet:info'].selected = false;
                    gnSearchManagerService.unselect(md['geonet:info'].uuid)
                      .then(updateSelectionNumber);
                  }
                }
                else {
                  // TODO: clear selection ?
                  console.log('Single selection is not ' +
                      'supported in remote mode.');
                  //  md['geonet:info'].selected = true;
                  //  gnSearchManagerService.select(md['geonet:info'].uuid)
                  //  .then(updateSelectionNumber);
                }
              };
            }
          }

          var updateSelectionNumber = function(data) {
            scope.selection = {
              length: data[0]
            };
          };

          scope.selectAll = function(all) {
            angular.forEach(scope.searchResults.records, function(md) {
              md['geonet:info'].selected = all;
            });
            if (all) {
              gnSearchManagerService.selectAll().then(updateSelectionNumber);
            } else {
              gnSearchManagerService.selectNone().then(updateSelectionNumber);
            }
          };

          /**
           * If local, selection is handled in an array on the client
           * if not, selection is handled on server side and
           * search results contains information if a record is selected or not.
           */
          scope.isSelected = function(md) {
            if (!scope.options.selection || !scope.options.selection.mode) {
              return false;
            }
            var targetUuid = md['geonet:info'].uuid;
            var selected = false;
            if (scope.options.selection.mode.indexOf('local') >= 0) {
              angular.forEach(scope.selection, function(md) {
                if (md['geonet:info'].uuid === targetUuid) {
                  selected = true;
                }
              });
            } else {
              selected = md['geonet:info'].selected;
            }
            return selected;
          };

          scope.$on('resetSelection', function(evt) {
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
    }]);
})();
