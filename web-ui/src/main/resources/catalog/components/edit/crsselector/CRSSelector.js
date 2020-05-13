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
  goog.provide('gn_crs_selector');

  var module = angular.module('gn_crs_selector', ['pascalprecht.translate']);

  /**
   *
   *
   */
  module.directive('gnCrsSelector',
      ['$rootScope', '$timeout', '$http',
       'gnEditor', 'gnEditorXMLService', 'gnCurrentEdit', '$translate',
       function($rootScope, $timeout, $http,
               gnEditor, gnEditorXMLService, gnCurrentEdit, $translate) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnCrsSelector',
             elementName: '@',
             elementRef: '@',
             domId: '@'
           },
           templateUrl: '../../catalog/components/edit/' +
           'crsselector/partials/' +
           'crsselector.html',
           link: function(scope, element, attrs) {
             scope.snippet = null;
             scope.crsResults = [];
             scope.snippetRef = gnEditor.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             // Replace the name attribute with id since this textarea is
             // used only to store the template, we don't wanna submit it
             var textarea = $.find('textarea[name=' + scope.snippetRef +
             ']')[0];
             var elemValue = $(textarea).attr('name');
             $(textarea).removeAttr('name');
             $(textarea).attr('id', elemValue);

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
               scope.elementRef, scope.elementName, scope.domId, 'before');
               return false;
             };


             scope.search = function() {
               if (scope.filter) {
                 $http.get('../api/registries/crs?type=&rows=50&q=' +
                 scope.filter).success(
                 function(data) {
                   scope.crsResults = data;
                 });
               }
             };

             // Then register search filter change
             scope.$watch('filter', scope.search);

             scope.addCRS = function(crs) {

               var textarea = $.find('textarea[id=' + scope.snippetRef +
               ']')[0];
               var xmlSnippet = textarea ? $(textarea).text() : undefined;
               scope.snippet = gnEditorXMLService.buildCRSXML(crs,
               gnCurrentEdit.schema, xmlSnippet);
               scope.crsResults = [];

               $timeout(function() {
                 // Save the metadata and refresh the form
                 gnEditor.save(gnCurrentEdit.id, true).then(function() {
                   // Success. Do nothing.
                 }, function(rejectedValue) {
                   $rootScope.$broadcast('StatusUpdated', {
                     title: $translate.instant('runServiceError'),
                     error: rejectedValue,
                     timeout: 0,
                     type: 'danger'
                   });
                 });
               });

               return false;
             };
           }
         };
       }]);
})();
