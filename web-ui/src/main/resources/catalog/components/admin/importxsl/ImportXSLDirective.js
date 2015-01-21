(function() {
  goog.provide('gn_importxsl_directive');

  var module = angular.module('gn_importxsl_directive', []);

  /**
   * Provide a list of available XSLT transformation
   *
   */
  module.directive('gnImportXsl', ['$http', '$translate',
    function($http, $translate) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          element: '=gnImportXsl'
        },
        templateUrl: '../../catalog/components/admin/importxsl/partials/' +
            'importxsl.html',
        link: function(scope, element, attrs) {
          $http.get('admin.harvester.info?' +
              'type=importStylesheets&_content_type=json')
            .success(function(data) {
                scope.stylesheets = data[0];
                scope.stylesheets.unshift({
                  id: '',
                  name: ''
                });
                scope.element = '';
              });
        }
      };
    }]);
})();
