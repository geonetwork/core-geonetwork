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
  goog.provide('gn_record_fragment_selector');

  var module = angular.module('gn_record_fragment_selector', []);

  /**
   * Select a fragment from another metadata record
   * and copy it in current one. Fragments are extracted using
   * SavedQuery API.
   */
  module.directive('gnRecordFragmentSelector',
      ['$http', 'gnGlobalSettings',
       'gnEditor', 'gnEditorXMLService', 'gnCurrentEdit',
       function($http, gnGlobalSettings,
               gnEditor, gnEditorXMLService, gnCurrentEdit) {

         return {
           restrict: 'A',
           replace: true,
           scope: {
             mode: '@gnRecordFragmentSelector',
             elementName: '@',
             elementRef: '@',
             domId: '@'
           },
           templateUrl: '../../catalog/components/edit/' +
           'recordfragmentselector/partials/' +
           'rfselector.html',
           compile: function compile(tElement, tAttrs, transclude) {
             return {
               pre: function preLink(scope) {
                 scope.searchObj = {
                   any: '',
                   internal: true,
                   params: {
                     any: '',
                     from: 1,
                     to: 50,
                     sortBy: 'title',
                     sortOrder: 'reverse'
                   }
                 };

                 scope.modelOptions = angular.copy(
                  gnGlobalSettings.modelOptions);
               },
               post: function(scope, element, attrs) {
                 scope.snippet = null;
                 scope.sourceRecord = null;

                 // Source records define a list of UUIDs to choose from
                 // TODO: Retrieve title of source records
                 // to be displayed in the selector.
                 scope.sourceRecords =
                  (attrs['sourceRecords'] &&
                 attrs['sourceRecords'].split(',')) || [];

                 scope.exclude =
                  (attrs['exclude'] &&
                 attrs['exclude'].split('#')) || [];

                 // Define a search query to choose from
                 if (scope.sourceRecords.length > 0) {
                   scope.searchQuery = {
                     _uuid: scope.sourceRecords.join(' or ')
                   };
                 } else {
                   scope.searchQuery =
                    (attrs['searchQuery'] &&
                   angular.fromJson(
                        attrs['searchQuery']
                   .replace('{uuid}', gnCurrentEdit.uuid)
                   .replace(/'/g, '\"')
                   )) || {};
                 }

                 angular.extend(scope.searchObj.params, scope.searchQuery);

                 scope.query = 'dq-sections';
                 scope.snippetRef = gnEditor.
                 buildXMLFieldName(scope.elementRef, scope.elementName);

                 scope.setSource = function(r) {
                   if (angular.isObject(r)) {
                     scope.sourceRecordTitle = r.title || r.defaultTitle || '';
                     scope.sourceRecord = r['geonet:info'].uuid;
                   } else {
                     scope.sourceRecordTitle = null;
                     scope.sourceRecord = null;
                     scope.fragments = {};
                   }

                 };
                 scope.getFragments = function() {
                   scope.fragments = [];
                   $http.post(
                    '../api/0.1/records/' + scope.sourceRecord +
                    '/query/' + scope.query, {}).then(function(r) {
                     if (r.status === 200) {
                       scope.fragments = {};
                       if (scope.exclude.length > 0) {
                         angular.forEach(r.data, function(value, key) {
                           if (scope.exclude.indexOf(key.trim()) === -1) {
                             scope.fragments[key.trim()] = value;
                           }
                         });
                       } else {
                         scope.fragments = r.data;
                       }
                     }
                   });
                 };

                 // Append * for like search
                 scope.updateParams = function() {
                   scope.searchObj.params.any =
                    '*' + scope.searchObj.any + '*';
                 };

                 scope.$watch('sourceRecord', function(n, o) {
                   if (n && n !== o) {
                     scope.getFragments();
                   }
                 });

                 scope.add = function() {
                   gnEditor.add(gnCurrentEdit.id,
                    scope.elementRef, scope.elementName, scope.domId, 'before');
                   return false;
                 };

                 scope.addFragment = function(f) {
                   var field = $.find('input[name=' + scope.snippetRef +
                    ']')[0];

                   $(field).val(f);
                   scope.fragments = [];
                   gnEditor.save(gnCurrentEdit.id, true);
                   return false;
                 };

               }
             };
           }
         }}]);
})();
