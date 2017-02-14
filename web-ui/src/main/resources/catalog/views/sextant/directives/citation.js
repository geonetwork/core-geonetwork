(function() {

  goog.provide('sxt_citation');

  var module = angular.module('sxt_citation', []);


  module.directive('sxtCitation', [
    function() {
      return {
        restrict: 'E',
        scope: {
          md: '<metadata'
        },
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/citation.html',
        link: function(scope) {
          var doi = scope.md.getLinksByType('WWW:LINK-1.0-http--metadata-URL');
          if(doi) {
            scope.doi = doi[0];
          }
        }
      };
    }
  ]);
})();
