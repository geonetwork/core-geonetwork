(function() {
  goog.provide('gn_harvestervalidation_directive');

  var module = angular.module('gn_harvestervalidation_directive', []);

  /**
   * Provide a list of available XSLT transformation
   *
   */
  module.directive('gnHarvesterValidation', ['$http', function($http) {

    return {
      restrict: 'A',
      replace: true,
      transclude: true,
      scope: {
        element: '=gnHarvesterValidation'
      },
      templateUrl: '../../catalog/components/admin/harvester/partials/' +
          'harvestervalidation.html',
      link: function(scope, element, attrs) {
        $http.get('admin.harvester.info?type=validation&_content_type=json')
            .success(function(data) {
              scope.validation = data[0];
            });
      }
    };
  }]);
})();
