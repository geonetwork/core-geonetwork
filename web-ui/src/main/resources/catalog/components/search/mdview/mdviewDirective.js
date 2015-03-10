(function() {
  goog.provide('gn_mdview_directive');

  var module = angular.module('gn_mdview_directive', [
    'ui.bootstrap.tpls',
    'ui.bootstrap.rating']);

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

          element.on('click', function(e) {
            e.preventDefault();
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

  module.directive('gnMetadataRate', [
    '$http',
    function($http) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'rate.html',
        restrict: 'A',
        scope: {
          md: '=gnMetadataRate',
          readonly: '@readonly'
        },

        link: function(scope, element, attrs, controller) {
          scope.$watch('md', function() {
            scope.rate = scope.md ? scope.md.rating : null;
          });

          if (!scope.readonly) {
            scope.$watch('rate', function(value, oldValue) {
              if (value) {
                return $http.get('md.rate?_content_type=json&' +
                    'uuid=' + scope.md['geonet:info'].uuid +
                    '&rating=' + value).success(function(data) {
                  scope.rate = data[0];
                });
              }
            });
          }
        }
      };
    }]
  );
})();
