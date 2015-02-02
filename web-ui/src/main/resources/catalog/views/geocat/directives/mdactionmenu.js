(function() {

  goog.provide('gn_search_geocat_mdactionmenu');

  var module = angular.module('gn_search_geocat_mdactionmenu', []);


  module.directive('gcMdActionsMenu', ['gnMetadataActions',
    function(gnMetadataActions) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/geocat/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gcMdActionsMenu);
        }
      };
    }
  ]);
})();
