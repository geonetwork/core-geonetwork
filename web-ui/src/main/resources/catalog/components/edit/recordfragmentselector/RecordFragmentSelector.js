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
      ['$http',
       'gnEditor', 'gnEditorXMLService', 'gnCurrentEdit',
       function($http,
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
           link: function(scope, element, attrs) {
             scope.snippet = null;

             // TODO: Retrieve title of source records
             // to be displayed in the selector.
             scope.sourceRecords =
             (attrs['sourceRecords'] &&
             attrs['sourceRecords'].split(',')) || [];
             scope.query = 'dq-sections';
             scope.snippetRef = gnEditor.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.getFragments = function() {
               scope.fragments = [];
               $http.post(
               '../api/0.1/records/' + scope.sourceRecord +
               '/query/' + scope.query, {}).then(function(r) {
                 if (r.status === 200) {
                   scope.fragments = r.data;
                 }
               });
             };

             scope.setSource = function(r) {
               scope.sourceRecord = r;
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

             scope.search = function() {
               if (angular.isUndefined(scope.sourceRecord)) {
                 //
               }
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
       }]);
})();
