(function() {

  goog.provide('sxt_citation');

  var module = angular.module('sxt_citation', []);


  module.directive('sxtCitation', [ 'gnSearchSettings', 'gnViewerSettings',
    function(searchSettings, viewerSettings) {
      return {
        restrict: 'E',
        scope: {
          md: '<metadata'
        },
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/citation.html',
        link: function(scope) {
          var doi = scope.md.getLinksByType('application/vnd.ogc.wms_xml');
          if(doi) {
            scope.doi = doi[0];
          }
        }
      };
    }
  ]);
})();
