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
    'gnMdView', 'gnSearchSettings', function(gnMdView, gnSearchSettings) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'mdpanel.html',
        scope: true,
        link: function(scope, element, attrs, controller) {

          var unRegister;

          scope.dismiss = function() {
            unRegister();
            gnMdView.removeLocationUuid();
            element.remove();
            //TODO: is the scope destroyed ?
          };

          if (gnSearchSettings.dismissMdView) {
            scope.dismiss = gnSearchSettings.dismissMdView;
          }
          unRegister = scope.$on('locationBackToSearchFromMdview', function() {
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


          scope.rateForRecord = function() {
            return $http.get('md.rate?_content_type=json&' +
                'uuid=' + scope.md['geonet:info'].uuid +
                '&rating=' + scope.rate).success(function(data) {
              scope.rate = data[0];
            });
          };
        }
      };
    }]
  );
})();
