(function() {
  goog.provide('gn_search_form_results_directive');

  var module = angular.module('gn_search_form_results_directive', []);

  module.directive('gnSearchFormResults', [
    'gnMetadataManagerService',
    function(gnMetadataManagerService) {

      var activeClass = 'active';

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/search/searchmanager/partials/' +
            'searchresults.html',
        scope: {
          resultRecords: '=',
          selection: '=selectRecords'
        },
        link: function(scope, element, attrs) {

          // get init options
          scope.options = {};
          jQuery.extend(scope.options, {
            mode: attrs.gnSearchFormResultsMode,
            selection: {
              mode: attrs.gnSearchFormResultsSelectionMode
            }
          });

          if (scope.options.selection) {
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
            }
          }
        }
      };
    }]);
})();
