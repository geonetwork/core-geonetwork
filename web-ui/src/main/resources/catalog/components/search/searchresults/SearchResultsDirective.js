(function() {
  goog.provide('gn_search_results_directive');

  var module = angular.module('gn_search_results_directive', []);

  module.directive('gnSearchResults', ['gnSearchManagerService',
                                       function(gnSearchManagerService) {

      var activeClass = 'active';

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          allowSelection: '@allowSelection',
          authenticated: '=authenticated',
          selectedRecordsCount: '=selectedRecordsCount',
          searchResults: '=gnSearchResults'
        },
        templateUrl: '../../catalog/components/search/searchresults/partials/' +
            'searchresults.html',
        link: function(scope, element, attrs) {
          scope.select = function(md, e) {
            var uuid = md['geonet:info'].uuid;
            var fn = $('#gn-record-' + uuid).hasClass(activeClass) ?
                gnSearchManagerService.unselect :
                gnSearchManagerService.select;
            fn(uuid).then(function(data) {
              scope.selectedRecordsCount = data[0];
              md['geonet:info'].selected = md['geonet:info'].selected !== true;
            });
          };
          scope.selectAll = function(all) {
            var fn = all ?
                gnSearchManagerService.selectAll :
                gnSearchManagerService.selectNone;
            fn().then(function(data) {
              scope.selectedRecordsCount = data[0];
              angular.forEach(scope.searchResults.metadata, function(md) {
                md['geonet:info'].selected = all ? true : false;
              });
            });
          };
          scope.view = function(md, e) {
            gnSearchManagerService.view(md);
          };
          scope.edit = function(md, e) {
            gnSearchManagerService.edit(md);
          };
        }
      };
    }]);
})();
