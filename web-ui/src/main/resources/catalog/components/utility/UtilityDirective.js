(function() {
  goog.provide('gn_utility_directive');

  goog.require('gn_schema_manager_service');

  angular.module('gn_utility_directive', [
  ])
  .directive('groupsCombo', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/templates/utils/groupsCombo.html',
            scope: {
              ownerGroup: '=',
              lang: '='
            },
            link: function(scope, element, attrs) {
              $http.get('admin.group.list@json').success(function(data) {
                scope.groups = data !== 'null' ? data : null;
              });
            }
          };
        }])
  .directive('protocolsCombo', ['$http', 'gnSchemaManagerService',
        function($http, gnSchemaManagerService) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/templates/utils/protocolsCombo.html',
            scope: {
              protocol: '=',
              lang: '='
            },
            controller: ['$scope', '$translate', function($scope, $translate) {
              var config = 'iso19139|gmd:protocol|||';
              gnSchemaManagerService.getTooltip(config).then(function(data) {
                $scope.protocols = data !== 'null' ?
                    data[0].helper.option : null;
              });
            }]
          };
        }]);
})();
