(function() {
  goog.provide('gn_mdview_directive');

  var module = angular.module('gn_mdview_directive', [])

  module.directive('gnMetadataOpen',
    ['$http', '$sanitize', '$compile', 'gnSearchSettings', '$sce',
      function($http, $sanitize, $compile, gnSearchSettings, $sce) {
        return {
          restrict: 'A',
          scope: {
            md: '=gnMetadataOpen',
            selector: '@gnMetadataOpenSelector'
          },

          link: function(scope, element, attrs, controller) {
            element.on('click', function() {
              var URI = gnSearchSettings.formatter.defaultUrl;
              $http.get(URI + scope.md.getUuid()).then(function(response) {
                scope.fragment = $sce.trustAsHtml(response.data);
                var el = document.createElement('div');
                el.setAttribute('gn-metadata-display', '');
                $(scope.selector).append(el);
                $compile(el)(scope);
              });
            });
          }
        };
      }]
  );

  module.directive('gnMetadataDisplay', ['$timeout', function($timeout) {
    return {
      templateUrl: '../../catalog/components/search/mdview/partials/' +
          'mdpanel.html',
      link: function(scope, element, attrs, controller) {
        scope.dismiss = function() {
          element.remove();
        };

        scope.$on('closeMdView', function() {
          scope.dismiss();
        });
      }
    };
  }]);

})();
