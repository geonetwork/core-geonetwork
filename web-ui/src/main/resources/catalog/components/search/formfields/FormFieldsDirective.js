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
              var url = 'info@json?type=groupsIncludingSystemGroups';
              if (attrs.profile) {
                url = 'info@json?type=groups&profile=' + attrs.profile;
              }
              $http.get(url, {cache: true}).
                  success(function(data) {
                    scope.groups = data !== 'null' ? data.group : null;

                    // Select by default the first group.
                    if ((angular.isUndefined(scope.ownerGroup) ||
                         scope.ownerGroup === '') && data.group) {
                      scope.ownerGroup = data.group[0]['@id'];
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

      /**
   * @ngdoc directive
   * @name gn_form_fields_directive.directive:schemaInfoCombo
   * @restrict A
   * @requires gnSchemaManagerService
   * @requires $http
   * @requires gnCurrentEdit
   *
   * @description
   * The `schemaInfoCombo` directive provides a combo box based on
   * a codelist (<schema>/loc/<lang>/codelist.xml) or an
   * element helper (<schema>/loc/<lang>/helper.xml) from a
   * schema plugins.
   *
   * The combo initialization is made on mouseover in order to not
   * link all combos (including hidden one) on load.
   *
   * The gn-schema-info-combo attribute can contains one of the
   * registered element (which are profile dependant ie. protocol,
   * associationType, initiativeType) or any element name with
   * namespace prefix eg. 'gmd:protocol'.
   *
   * The schema used to retrieve the element info is based on
   * the gnCurrentEdit object or 'iso19139' if not defined.
   */
  .directive('schemaInfoCombo', ['$http', 'gnSchemaManagerService',
        'gnCurrentEdit', 'gnElementsMap',
        function($http, gnSchemaManagerService,
                 gnCurrentEdit, gnElementsMap) {
          return {
            restrict: 'A',
            replace: true,
            templateUrl: '../../catalog/components/search/formfields/' +
                'partials/schemainfocombo.html',
            scope: {
              selectedInfo: '=',
              lang: '=',
              allowBlank: '@'
            },
            link: function(scope, element, attrs) {
              var initialized = false;
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


              var init = function() {
                var schema = gnCurrentEdit.schema || 'iso19139';
                var element = (gnElementsMap[attrs['gnSchemaInfo']] &&
                    gnElementsMap[attrs['gnSchemaInfo']][schema]) ||
                    attrs['gnSchemaInfo'];
                var config = schema + '|' + element + '|||';

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
                          angular.forEach(data[0].helper.option ||
                              data[0].helper,
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
                initialized = true;
              };
              element.bind('mouseover', function() {
                if (!initialized) {
                  init();
                }
              });
            }
          };
        }]);
})();
