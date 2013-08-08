(function() {
    goog.provide('gn_search_results_directive');

    var module = angular.module('gn_search_results_directive', []);

    module.directive('gnSearchResults', ['gnMetadataManagerService', function(gnMetadataManagerService) {
        
        var activeClass = 'active';
        
        return {
            restrict : 'A',
            replace: true,
            transclude: true,
            scope: { 
                allowSelection: '@gnSearchResultsAllowSelection',
                selectedRecordsCount: '=gnSearchResultsSelectedRecordsCount',
                searchResults: '=gnSearchResults'
            },
            templateUrl: '../../catalog/components/searchresults/partials/' +
              'searchresults.html',
            link : function(scope, element, attrs) {
                scope.select = function (uuid, e) {
                    var fn = $(e.target).toggleClass(activeClass).hasClass(activeClass) ? 
                                gnMetadataManagerService.select : 
                                gnMetadataManagerService.unselect;
                    fn(uuid).then(function (data) {
                        scope.selectedRecordsCount = data[0];
                    });
                }
                scope.selectAll = function (all) {
                    var fn = all ? 
                            gnMetadataManagerService.selectAll : 
                            gnMetadataManagerService.selectNone;
                    fn().then(function (data) {
                        scope.selectedRecordsCount = data[0];
                        element.find('.gn-record').toggleClass(activeClass, all);
                        // Tested with checkbox for selection
                        // For some obscur reason, the following checked and
                        // unchecked properly only one time ?
                        // element.find('input:checkbox').attr('checked', e.target.checked);
//                        element.find('input:checkbox').each(function (i, checkbox) {
//                          checkbox.checked = all;
//                        });
                    });
                }
            }
        };
    }]);
})();
