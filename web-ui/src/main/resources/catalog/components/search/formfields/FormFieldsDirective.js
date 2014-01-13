(function() {
  goog.provide('gn_form_fields_directive');

  angular.module('gn_form_fields_directive', [
  ])
  .directive('groupsCombo', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/groupsCombo.html',
            scope: {
              ownerGroup: '=',
              lang: '=',
              groups: '='
            },
            link: function(scope, element, attrs) {
              $http.get('admin.group.list@json').success(function(data) {
                scope.groups = data !== 'null' ? data : null;

                // Select by default the first group.
                if (scope.ownerGroup === '' && data) {
                  scope.ownerGroup = data[0]['id'];
                }
              });
            }
          };
        }])
  .directive('protocolsCombo', ['$http', 'gnSchemaManagerService',
        function($http, gnSchemaManagerService) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/protocolsCombo.html',
            scope: {
              protocol: '=',
              lang: '='
            },
            controller: ['$scope', '$translate', function($scope, $translate) {
              var config = 'iso19139|gmd:protocol|||';
              gnSchemaManagerService.getElementInfo(config).then(
                  function(data) {
                    $scope.protocols = data !== 'null' ?
                        data[0].helper.option : null;
                  });
            }]
          };
        }])
  .directive('schemaInfoCombo', ['$http', 'gnSchemaManagerService',
        function($http, gnSchemaManagerService) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/schemainfocombo.html',
            scope: {
              selectedInfo: '=',
              lang: '='
            },
            link: function(scope, element, attrs) {
              var config = 'iso19139|' + attrs['gnSchemaInfo'] + '|||';
              scope.type = attrs['schemaInfoCombo'];
              if (scope.type == 'codelist') {
                gnSchemaManagerService.getCodelist(config).then(
                    function(data) {
                      scope.infos = data !== 'null' ?
                          data[0].entry : null;
                    });
              }
              else if (scope.type == 'element') {
                gnSchemaManagerService.getElementInfo(config).then(
                    function(data) {
                      scope.infos = data !== 'null' ?
                          data[0].helper.option : null;
                    });
              }
            }
          };
        }]);

})();
