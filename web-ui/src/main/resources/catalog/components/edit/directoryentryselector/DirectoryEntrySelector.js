/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_directory_entry_selector');



  goog.require('gn_editor_xml_service');
  goog.require('gn_metadata_manager_service');
  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_directory_entry_selector',
      ['gn_metadata_manager_service', 'gn_schema_manager_service',
        'gn_editor_xml_service', 'pascalprecht.translate']);

  /**
   *
   *
   */
  module.directive('gnDirectoryEntrySelector',
      ['$rootScope', '$timeout', '$q', '$http', '$translate',
        'gnEditor', 'gnSchemaManagerService',
        'gnEditorXMLService', 'gnHttp', 'gnConfig',
        'gnCurrentEdit', 'gnConfigService', 'gnPopup',
        'gnGlobalSettings',
        function($rootScope, $timeout, $q, $http, $translate,
                 gnEditor, gnSchemaManagerService,
                 gnEditorXMLService, gnHttp, gnConfig,
                 gnCurrentEdit, gnConfigService, gnPopup,
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
              transformation: '@',
              // If not using the directive in an editor context, set
              // the schema id to properly retrieve the codelists.
              schema: '@',
              selectEntryCb: '='
            },
            templateUrl: '../../catalog/components/edit/' +
                'directoryentryselector/partials/' +
                'directoryentryselector.html',

            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope) {
                  scope.searchObj = {
                    any: '',
                    params: {
                      _isTemplate: 's',
                      any: '',
                      from: 1,
                      to: 20,
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
                 (gnConfig[gnConfig.key.isXLinkLocal] === true ?
                      'local://' : gnConfigService.getServiceURL()) +
                 'api/registries/entries/';
                  scope.gnConfig = gnConfig;
                  // If true, display button to add the element
                  // without using the subtemplate selector.
                  scope.templateAddAction = iAttrs.templateAddAction == 'true';
                  // If true, display input to search with autocompletion
                  scope.searchAction = iAttrs.searchAction == 'true';
                  // If true, display button to search using the popup selector
                  scope.popupAction = iAttrs.popupAction == 'true';
                  scope.isContact = scope.templateType === 'contact';
                  scope.hasDynamicVariable = scope.variables &&
                      scope.variables.match('{.*}') !== null;

                  // Search only for contact subtemplate
                  // by default.
                  if (scope.filter) {
                    angular.extend(scope.searchObj.params,
                        angular.fromJson(scope.filter));
                  }

                  // Append * for like search
                  scope.updateParams = function() {
                    scope.searchObj.params.any =
                   '*' + scope.searchObj.any + '*';
                  };

                  scope.snippet = null;
                  scope.snippetRef = gnEditor.
                      buildXMLFieldName(scope.elementRef, scope.elementName);

                  scope.attrs = iAttrs;
                  scope.add = function() {
                    return gnEditor.add(gnCurrentEdit.id,
                        scope.elementRef, scope.elementName,
                        scope.domId, 'before').then(function() {
                      if (scope.templateAddAction) {
                        gnEditor.save(gnCurrentEdit.id, true).then(function() {
                          // success. Nothing to do.
                        }, function(rejectedValue) {
                          $rootScope.$broadcast('StatusUpdated', {
                            title: $translate.instant('saveMetadataError'),
                            error: rejectedValue,
                            timeout: 0,
                            type: 'danger'
                          });
                        });
                      }
                    });
                  };

                  // <request><codelist schema="iso19139"
                  // name="gmd:CI_RoleCode" /></request>
                  scope.addEntry = function(entry, role, usingXlink) {
                    var defer = $q.defer();
                    gnCurrentEdit.working = true;
                    if (!(entry instanceof Array)) {
                      entry = [entry];
                    }

                    scope.snippet = '';
                    var snippets = [];

                    var checkState = function(c) {
                      if (snippets.length === entry.length) {
                        scope.snippet = snippets.join(separator);
                        // Clean results
                        // TODO: should call clean result from
                        // searchFormController
                        //                   scope.searchResults.records = null;
                        //                   scope.searchResults.count = null;
                        // Only if editing.
                        if (gnCurrentEdit.id) {
                          $timeout(function() {
                            // Save the metadata and refresh the form
                            gnEditor.save(gnCurrentEdit.id, true).then(
                           function(r) {
                             defer.resolve(r);
                           }, function(rejectedValue) {
                             $rootScope.$broadcast('StatusUpdated', {
                               title: $translate.instant('runServiceError'),
                               error: rejectedValue,
                               timeout: 0,
                               type: 'danger'
                             });
                           });
                          });
                        }
                        if (angular.isFunction(scope.selectEntryCb)) {
                          scope.selectEntryCb(scope, c, role);
                        }
                      }
                    };

                    angular.forEach(entry, function(c) {
                      var id = c['geonet:info'].id,
                          uuid = c['geonet:info'].uuid;
                      var params = [];

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
                        params.push('process=' +
                            scope.variables.replace('{role}', role));
                      } else if (scope.variables) {
                        params.push('process=' + scope.variables);
                      }

                      if (angular.isString(scope.transformation) &&
                          scope.transformation !== '') {
                        params.push('transformation=' + scope.transformation);
                      }

                      var urlParams = params.join('&');
                      $http.get(
                          '../api/registries/entries/' + uuid + '?' + urlParams)
                     .success(function(xml) {
                       if (usingXlink) {
                         snippets.push(gnEditorXMLService.
                                  buildXMLForXlink(scope.schema,
                         scope.elementName,
                                      url + uuid +
                                      '?' + urlParams));
                       } else {
                         snippets.push(gnEditorXMLService.
                                  buildXML(scope.schema,
                         scope.elementName, xml));
                       }
                       checkState(c);
                     });
                    });

                    return defer.promise;
                  };

                  var schemaId = gnCurrentEdit.schema || scope.schema;
                  gnSchemaManagerService
                     .getCodelist(schemaId + '|' + 'roleCode')
                      .then(function(data) {
                        scope.roles = data.entry;
                      });

                  scope.openSelector = function() {
                    openModal({
                      title: $translate.instant('chooseEntry'),
                      content:
                     '<div gn-directory-entry-list-selector=""></div>',
                      class: 'gn-modal-lg'
                    }, scope, 'EntrySelected');
                  };
                  var popup;
                  var openModal = function(o, scope) {
                    popup = gnPopup.createModal(o, scope);
                  };
                  scope.closeModal = function() {
                    popup.trigger('hidden.bs.modal');
                  };
                }
              };
            }
          };
        }]);

  module.directive('gnDirectoryEntryListSelector',
      ['gnGlobalSettings',
       function(gnGlobalSettings) {
         return {
           restrict: 'A',
           templateUrl: '../../catalog/components/edit/' +
           'directoryentryselector/partials/' +
           'directoryentrylistselector.html',

           compile: function compile(tElement, tAttrs, transclude) {
             return {
               pre: function preLink(scope) {
                 scope.searchObj = {
                   defaultParams: {
                     _isTemplate: 's',
                     any: '',
                     from: 1,
                     to: 10,
                     _root: 'gmd:CI_ResponsibleParty',
                     sortBy: 'title',
                     sortOrder: 'reverse',
                     resultType: 'contact'
                   }
                 };
                 scope.searchObj.params = angular.extend({},
                 scope.searchObj.defaultParams);
                 scope.stateObj = {
                   selectRecords: []
                 };
                 if (scope.filter) {
                   var filter = angular.fromJson(scope.filter);
                   angular.extend(scope.searchObj.params, filter);
                   angular.extend(scope.searchObj.defaultParams, filter);
                 }
                 scope.modelOptions = angular.copy(
                 gnGlobalSettings.modelOptions);
               },
               post: function postLink(scope, iElement, iAttrs) {
                 scope.defaultRoleCode = iAttrs['defaultRole'] || null;
                 scope.defaultRole = null;
                 angular.forEach(scope.roles, function(r) {
                   if (r.code == scope.defaultRoleCode) {
                     scope.defaultRole = r;
                   }
                 });
                 scope.addSelectedEntry = function(role, usingXlink) {
                   scope.addEntry(
                   scope.stateObj.selectRecords[0],
                   role,
                   usingXlink).then(function(r) {
                     scope.closeModal();
                   });
                 };
               }
             };
           }
         };
       }]);
})();
