(function() {
  goog.provide('gn_resultsview');

  var module = angular.module('gn_resultsview', []);

  module.directive('gnResultsTplSwitcher', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/search/resultsview/partials/' +
            'templateswitcher.html',
        scope: {
          'templateUrl': '='
        },
        link: function(scope, element, attrs, controller) {
          scope.tpls = [{
            tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/title.html',
            tooltip: 'Simple',
            icon: 'fa-list'
          }, {
            tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html',
            tooltip: 'Thumbnails',
            icon: 'fa-th-list'
          }]
        }
      };
    }]);
  module.directive('gnResultsContainer', [
      '$compile',
    function($compile) {

      return {
        restrict: 'A',
        scope: {
          searchResults: '=',
          'templateUrl': '='
        },
        link: function(scope, element, attrs, controller) {

          scope.$watch('templateUrl', function(templateUrl) {
            if (angular.isUndefined(templateUrl)) {
              return;
            }
            var template = angular.element(document.createElement('div'))
            template.attr({
              'ng-include': 'templateUrl'
            });
            element.empty();
            element.append(template);
            $compile(template)(scope);
          });
        }
      };
    }]);
})();
