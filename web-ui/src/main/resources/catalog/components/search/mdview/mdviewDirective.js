(function() {
  goog.provide('gn_mdview_directive');

  var module = angular.module('gn_mdview_directive', []);

  module.directive('gnMetadataOpen', [
    '$http',
    '$sanitize',
    '$compile',
    'gnSearchSettings',
    '$sce',
    'gnMdView',
    function($http, $sanitize, $compile, gnSearchSettings, $sce, gnMdView) {
      return {
        restrict: 'A',
        scope: {
          md: '=gnMetadataOpen',
          selector: '@gnMetadataOpenSelector'
        },

        link: function(scope, element, attrs, controller) {

          element.on('click', function() {
            gnMdView.setLocationUuid(scope.md.getUuid());
            scope.$apply();
          });
        }
      };
    }]
  );

  module.directive('gnMetadataDisplay', [
    'gnMdView', function(gnMdView) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'mdpanel.html',
        scope: true,
        link: function(scope, element, attrs, controller) {
          scope.dismiss = function() {
            gnMdView.removeLocationUuid();
            element.remove();
            //TODO: is the scope destroyed ?
          };

          scope.$on('closeMdView', function() {
            scope.dismiss();
          });
        }
      };
    }]);

})();
