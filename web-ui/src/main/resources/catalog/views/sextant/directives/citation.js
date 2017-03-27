(function() {

  goog.provide('sxt_citation');

  var module = angular.module('sxt_citation', []);


  module.directive('sxtCitation', [ '$translate',
    function($translate) {
      return {
        restrict: 'E',
        scope: {
          md: '<metadata'
        },
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/citation.html',
        link: function(scope, element) {
          var doi = scope.md.getLinksByType('WWW:LINK-1.0-http--metadata-URL');
          if(doi) {
            scope.doi = doi[0];

            setTimeout(function() {
              var infoBt = element.find('.fa-info-circle').parent();
              infoBt.popover({
                content: $translate.instant('citationContent'),
                title: $translate.instant('citationTitle'),
                trigger: 'hover'
              });
            })
          }
        }
      };
    }
  ]);
})();
