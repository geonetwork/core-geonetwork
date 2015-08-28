(function() {
  goog.provide('gn_directory_entry_selector');



  goog.require('gn_editor_xml_service');
  goog.require('gn_metadata_manager_service');
  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_directory_entry_selector',
      ['gn_metadata_manager_service', 'gn_schema_manager_service',
        'gn_editor_xml_service']);

  /**
   *
   *
   */
  module.directive('gnDirectoryEntrySelector',
      ['$rootScope', '$timeout', '$q', '$http',
        'gnEditor', 'gnSchemaManagerService',
        'gnEditorXMLService', 'gnHttp', 'gnConfig',
        'gnCurrentEdit', 'gnConfigService', 'gnElementsMap',
        'gnGlobalSettings',
        function($rootScope, $timeout, $q, $http,
                 gnEditor, gnSchemaManagerService,
                 gnEditorXMLService, gnHttp, gnConfig,
                 gnCurrentEdit, gnConfigService, gnElementsMap,
                 gnGlobalSettings) {

          return {
            restrict: 'A',
            replace: false,
            transclude: true,
            scope: {
              mode: '@gnDirectoryEntrySelector',
              elementName: '@',
              elementRef: '@',
              domId: '@',
              // Contact subtemplates allows definition
              // of the contact role. For other cases
              // only add action is provided
              templateType: '@',
              // If true, display button to add the element
              // without using the subtemplate selector.
              templateAddAction: '@',
              // Search option to restrict the subtemplate
              // search query
              filter: '@',
              // Parameters to be send when the subtemplate
              // snippet is retrieved before insertion
              // into the metadata records.
              variables: '@',
              // An optional transformation applies to the subtemplate
              // This may be used when using an ISO19139 contact directory
              // in an ISO19115-3 records.
              transformation: '@'
            },
            templateUrl: '../../catalog/components/edit/' +
                'directoryentryselector/partials/' +
                'directoryentryselector.html',

            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope) {
                  scope.searchObj = {
                    params: {
                      _isTemplate: 's',
                      any: '',
                      from: 1,
                      to: 200,
                      _root: 'gmd:CI_ResponsibleParty',
                      sortBy: 'title',
                      sortOrder: 'reverse',
                      resultType: 'subtemplates'
                    }
                  };

                  scope.modelOptions = angular.copy(
                 gnGlobalSettings.modelOptions);
                },
                post: function postLink(scope, iElement, iAttrs) {
                  // Separator between each contact XML
                  // snippet
                  var separator = '&&&';

                  // Define type of XLinks: local:// or http:// based on
                  // catalog configuration.
                  var url =
                 gnConfig[gnConfig.key.isXLinkLocal] === true ?
                      'local://' + scope.$parent.lang + '/subtemplate' :
                      gnConfigService.getServiceURL() + scope.$parent.lang +
                      '/subtemplate';
                  scope.gnConfig = gnConfig;
                  scope.templateAddAction = scope.templateAddAction === 'true';
                  scope.isContact = scope.templateType === 'contact';
                  scope.hasDynamicVariable = scope.variables &&
                      scope.variables.match('{.*}') !== null;

                  // Search only for contact subtemplate
                  // by default.
                  if (scope.filter) {
                    angular.extend(scope.searchObj.params,
                        angular.fromJson(scope.filter));
                  }

                  scope.snippet = null;
                  scope.snippetRef = gnEditor.
                      buildXMLFieldName(scope.elementRef, scope.elementName);


                  scope.add = function() {
                    return gnEditor.add(gnCurrentEdit.id,
                        scope.elementRef, scope.elementName,
                        scope.domId, 'before').then(function() {
                     if (scope.templateAddAction) {
                       gnEditor.save(gnCurrentEdit.id, true);
                     }
                   });
                  };

                  // <request><codelist schema="iso19139"
                  // name="gmd:CI_RoleCode" /></request>
                  scope.addEntry = function(entry, role, usingXlink) {
                    gnCurrentEdit.working = true;
                    if (!(entry instanceof Array)) {
                      entry = [entry];
                    }

                    scope.snippet = '';
                    var snippets = [];

                    var checkState = function() {
                      if (snippets.length === entry.length) {
                        scope.snippet = snippets.join(separator);

                        // Clean results
                        // TODO: should call clean result from
                        // searchFormController
                        //                   scope.searchResults.records = null;
                        //                   scope.searchResults.count = null;

                        $timeout(function() {
                          // Save the metadata and refresh the form
                          gnEditor.save(gnCurrentEdit.id, true);
                        });
                      }
                    };

                    angular.forEach(entry, function(c) {
                      var id = c['geonet:info'].id,
                          uuid = c['geonet:info'].uuid;
                      var params = {uuid: uuid};

                      // For the time being only contact role
                      // could be substitute in directory entry
                      // selector. This is done using the process
                      // parameter of the get subtemplate service.
                      // eg. data-variables="gmd:role/gmd:CI_RoleCode
                      //   /@codeListValue~{role}"
                      // will set the role of the contact.
                      // TODO: this could be applicable not only to contact role
                      // No use case identified for now.
                      if (scope.hasDynamicVariable && role) {
                        params.process =
                            scope.variables.replace('{role}', role);
                      } else if (scope.variables) {
                        params.process = scope.variables;
                      } else {
                        params.process = '';
                      }

                      if (angular.isString(scope.transformation) &&
                          scope.transformation !== '') {
                        params.transformation = scope.transformation;
                      }

                      gnHttp.callService(
                          'subtemplate', params).success(function(xml) {
                       if (usingXlink) {
                         var urlParams = '';
                         angular.forEach(params, function(p, key) {
                           urlParams += key + '=' + p + '&';
                         });
                         snippets.push(gnEditorXMLService.
                                  buildXMLForXlink(scope.elementName,
                                      url +
                                      '?' + urlParams));
                       } else {
                         snippets.push(gnEditorXMLService.
                                  buildXML(scope.elementName, xml));
                       }
                       checkState();
                     });
                    });

                    return false;
                  };

                  gnSchemaManagerService
                      .getCodelist(gnCurrentEdit.schema + '|' +
                          gnElementsMap['roleCode'][gnCurrentEdit.schema])
                      .then(function(data) {
                        scope.roles = data[0].entry;
                      });



                }
              };
            }
          };
        }]);
})();
