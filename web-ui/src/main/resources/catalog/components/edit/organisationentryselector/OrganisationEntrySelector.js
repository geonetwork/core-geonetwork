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
  goog.provide('gn_organisation_entry_selector');



  goog.require('gn_editor_xml_service');
  goog.require('gn_metadata_manager_service');
  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_organisation_entry_selector',
      ['gn_metadata_manager_service', 'gn_schema_manager_service',
       'gn_editor_xml_service', 'pascalprecht.translate']);

  /**
   *
   *
   */
  module.directive('gnOrganisationEntrySelector',
      ['$rootScope', '$timeout', '$q', '$http',
        'gnEditor', 'gnSchemaManagerService',
        'gnEditorXMLService', 'gnHttp', 'gnConfig',
        'gnCurrentEdit', 'gnConfigService', '$translate',
        function($rootScope, $timeout, $q, $http,
            gnEditor, gnSchemaManagerService,
            gnEditorXMLService, gnHttp, gnConfig,
            gnCurrentEdit, gnConfigService, $translate) {

         return {
           restrict: 'A',
           replace: false,
           scope: {
             mode: '@gnOrganisationEntrySelector',
             schema: '@',
             elementName: '@',
             elementRef: '@',
             domId: '@',
             // If elementChoice is set to 'true' then we are displaying
             // this directory selector next to a choice button which has been
             // constructed by the form-builder so templateAddAction is ignored
             // and we use a different template - see getTemplateUrl below
             elementChoice: '@',
             tagName: '@',
             paramName: '@',
             templateAddAction: '@',
             root: '@'
           },
           template: '<div ng-include="getTemplateUrl()"></div>',
           link: function(scope, element, attrs) {
             // Separator between each org XML
             // snippet
             var separator = '&&&';
             // URL used for creating XLink. Could be good to have
             // that as settings to define local:// or http:// xlinks.
             var url = gnConfigService.getServiceURL() + 'eng/subtemplate';
             scope.gnConfig = gnConfig;
             scope.templateAddAction = scope.templateAddAction === 'true';

             var root = 'mcp:CI_Organisation';
             if (scope.root) root = scope.root;
             // Search only for subtemplate with root element set to root
             scope.params = {
               _root: root,
               _isTemplate: 's',
               fast: 'false'
             };

             // return template according to choice setting
             scope.getTemplateUrl = function() {
                if (scope.elementChoice == 'true') {
                 return '../../catalog/components/edit/' +
                     'organisationentryselector/partials/' +
                     'organisationentryselectorchoice.html';
                } else {
                 return '../../catalog/components/edit/' +
                     'organisationentryselector/partials/' +
                     'organisationentryselector.html';
                }
             };

             scope.snippet = null;
             scope.snippetRef = gnEditor.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
                   scope.elementRef, scope.elementName,
                   scope.domId, 'before').then(function() {
                 if (scope.templateAddAction) {
                   gnEditor.save(gnCurrentEdit.id, true).then(function() {
                     // success. Nothing to do.
                   }, function(rejectedValue) {
                     $rootScope.$broadcast('StatusUpdated', {
                       title: $translate.instant('runServiceError'),
                       error: rejectedValue,
                       timeout: 0,
                       type: 'danger'
                     });
                   });
                 }
               });
               return false;
             };

             scope.addOrganisation = function(org, usingXlink) {
               if (!(org instanceof Array)) {
                 org = [org];
               }

               scope.snippet = '';
               var snippets = [];

               var checkState = function() {
                 if (snippets.length === org.length) {
                   scope.snippet = snippets.join(separator);

                   // Clean results
                   // TODO: should call clean result from searchFormController
                   //                   scope.searchResults.records = null;
                   //                   scope.searchResults.count = null;

                    $timeout(function() {
                      // Save the metadata and refresh the form
                      gnEditor.save(gnCurrentEdit.id, true).then(function() {
                        // success. Nothing to do
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
               };

               angular.forEach(org, function(c) {
                 var uuid = c['geonet:info'].uuid;
                 var params = {uuid: uuid};

                 gnHttp.callService(
                     'subtemplate', params).success(function(xml) {
                   if (usingXlink) {
                     snippets.push(gnEditorXMLService.
                     buildXMLForXlink(scope.schema, scope.elementName,
                         url + '?uuid=' + uuid + '&process=' + params.process));
                   } else {
                     snippets.push(gnEditorXMLService.
                     buildXML(scope.schema, scope.elementName, xml));
                   }
                   checkState();
                 });
               });

               return false;
             };

           }
         };
       }]);
})();
