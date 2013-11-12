(function() {
  goog.provide('gn_onlinesrc_directive');

  goog.require('gn_utility');

  angular.module('gn_onlinesrc_directive', [
    'gn_utility'
  ])
  .directive('gnAddOnlinesrc', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/onlinesrc/' +
                'partials/addOnlinesrc.html',
            controller: ['$scope', '$translate', function($scope, $translate) {
              $http.get('admin.group.list@json').success(function(data) {
                $scope.groups = data !== 'null' ? data : null;
              });
            }]
          };
        }])
  .directive('gnLinkParentMd', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/onlinesrc/' +
                'partials/linkParentMd.html',
            controller: ['$scope', '$translate', function($scope, $translate) {
              $http.get('admin.group.list@json').success(function(data) {
                $scope.groups = data !== 'null' ? data : null;
              });
            }]
          };
        }]);
})();
