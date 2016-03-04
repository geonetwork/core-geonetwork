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

  var module = angular.module('gn_crs_selector', []);

  /**
   *
   *
   */
  module.directive('gnCrsSelector',
      ['$rootScope', '$timeout', '$http',
       'gnEditor', 'gnEditorXMLService', 'gnCurrentEdit',
       function($rootScope, $timeout, $http,
               gnEditor, gnEditorXMLService, gnCurrentEdit) {

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
             scope.snippetRef = gnEditor.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
               scope.elementRef, scope.elementName, scope.domId, 'before');
               return false;
             };


             scope.search = function() {
               if (scope.filter) {
                 $http.get('crs.search@json?type=&maxResults=50&name=' +
                 scope.filter).success(
                 function(data) {
                   scope.crsResults = data;
                 });
               }
             };

             // Then register search filter change
             scope.$watch('filter', scope.search);

             scope.addCRS = function(crs) {
               scope.snippet = gnEditorXMLService.buildCRSXML(
               crs,
               gnCurrentEdit.schema);
               scope.crsResults = [];

               $timeout(function() {
                 // Save the metadata and refresh the form
                 gnEditor.save(gnCurrentEdit.id, true);
               });

               return false;
             };
           }
         };
       }]);
})();
