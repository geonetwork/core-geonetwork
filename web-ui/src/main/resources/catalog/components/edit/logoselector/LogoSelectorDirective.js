(function() {
  goog.provide('gn_logo_selector_directive');

  var module = angular.module('gn_logo_selector_directive', []);

  /**
   * Add a toggle button with a list of logo next
   * to an input. Apply the directive to the parent
   * element of the input.
   */
  module.directive('gnLogoSelector', ['$http',
    function($http) {

      return {
        restrict: 'A',
        transclude: true,
        templateUrl: '../../catalog/components/edit/' +
            'logoselector/partials/logoselector.html',
        link: function(scope, element, attrs) {
          // TODO: Get path to image based
          scope.path = location.origin + '/' +
              location.pathname.split('/')[1] +
              '/images/harvesting/';
          scope.setLogo = function(i) {
            $(element).find('input').get(0).value = scope.path + i;
          };

          $http.get('admin.harvester.info@json?type=icons', {cache: true})
          .success(function(data) {
                scope.logos = data[0];
              });
        }
      };
    }]);
})();
