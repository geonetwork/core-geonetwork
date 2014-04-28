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
              $http.get('admin.group.list@json', {cache: true}).
                  success(function(data) {
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
              lang: '=',
              allowBlank: '@'
            },
            link: function(scope, element, attrs) {
              var config = 'iso19139|' + attrs['gnSchemaInfo'] + '|||';
              var defaultValue;
              var addBlankValueAndSetDefault = function() {
                var blank = {label: '', code: ''};
                if (scope.infos != null && scope.allowBlank !== undefined) {
                  scope.infos.unshift(blank);
                }
                // Search default value
                angular.forEach(scope.infos, function(h) {
                  if (h['default'] == 'true') {
                    defaultValue = h.code;
                  }
                });

                // If no blank value allowed select default or first
                // If no value defined, select defautl or blank one
                if (!angular.isDefined(scope.selectedInfo)) {
                  scope.selectedInfo = defaultValue || scope.infos[0].code;
                }
                // This will avoid to have undefined selected option
                // on top of the list.
              };

              scope.type = attrs['schemaInfoCombo'];
              if (scope.type == 'codelist') {
                gnSchemaManagerService.getCodelist(config).then(
                    function(data) {
                      if (data !== 'null') {
                        scope.infos = [];
                        angular.copy(data[0].entry, scope.infos);
                      } else {
                        scope.infos = data[0].entry;
                      }

                      addBlankValueAndSetDefault();
                    });
              }
              else if (scope.type == 'element') {
                gnSchemaManagerService.getElementInfo(config).then(
                    function(data) {
                      if (data !== 'null') {
                        scope.infos = [];
                        // Helper element may be embbeded in an option
                        // property when attributes are defined
                        angular.forEach(data[0].helper.option || data[0].helper,
                            function(h) {
                              scope.infos.push({
                                code: h['@value'],
                                label: h['#text'],
                                description: h['@title'] || '',
                                'default': h['@default'] == '' ?
                                    'true' : 'false'
                              });
                            });
                      } else {
                        scope.infos = null;
                      }
                      addBlankValueAndSetDefault();
                    });
              }
            }
          };
        }]);
})();
