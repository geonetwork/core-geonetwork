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
  goog.provide('gn_date_picker_directive');

  var module = angular.module('gn_date_picker_directive', []);

  /**
   *  Create a widget to handle date composed of
   *  a date input and a time input. It can only be
   *  used to create an ISO date. It hides the
   *  need of choosing from ISO type date or datetime.
   *
   *  It's also useful as html datetime input are not
   *  yet widely supported.
   */
  module.directive('gnDatePicker',
      ['$http', '$rootScope', '$filter', '$timeout',
       'gnSchemaManagerService', 'gnCurrentEdit',
       function($http, $rootScope, $filter, $timeout,
                gnSchemaManagerService, gnCurrentEdit) {

         return {
           restrict: 'A',
           scope: {
             value: '@gnDatePicker',
             label: '@',
             elementName: '@',
             elementRef: '@',
             id: '@',
             tagName: '@',
             indeterminatePosition: '@',
             required: '@',
             hideTime: '@'
           },
           templateUrl: '../../catalog/components/edit/datepicker/partials/' +
           'datepicker.html',
           link: function(scope, element, attrs) {
             // Check if browser support date type or not to
             // HTML date and time input types.
             // If not datetimepicker.js is used (it will not
             // support year or month only mode in this case)
             scope.dateTypeSupported = Modernizr.inputtypes.date;
             scope.isValidDate = true;
             scope.hideTime = scope.hideTime == 'true';
             var datePattern = new RegExp('^\\d{4}$|' +
             '^\\d{4}-\\d{2}$|' +
             '^\\d{4}-\\d{2}-\\d{2}$|' +
             '^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$');
             // Format date when datetimepicker is used.
             scope.formatFromDatePicker = function(date) {
               var format = 'YYYY-MM-DDTHH:mm:ss';
               var dateTime = moment(date);
               scope.dateInput = dateTime.format(format);
             };

             scope.mode = scope.year = scope.month = scope.time =
             scope.date = scope.dateDropDownInput = '';
             scope.withIndeterminatePosition =
             attrs.indeterminatePosition !== undefined;

             // Default date is empty
             // Compute mode based on date length. The format
             // is always ISO YYYY-MM-DDTHH:mm:ss
             if (!scope.value) {
               scope.value = '';
             } else if (scope.value.length === 4) {
               scope.year = parseInt(scope.value);
               scope.mode = 'year';
             } else if (scope.value.length === 7) {
               scope.month = moment(scope.value, 'YYYY-MM').toDate();
               scope.mode = 'month';
             } else {
               var isDateTime = scope.value.indexOf('T') !== -1;
               var tokens = scope.value.split('T');
               scope.date = new Date(moment(isDateTime ? tokens[0] : scope.value).utc().format());
               scope.time = isDateTime ?
               moment(tokens[1], 'HH:mm:ss').toDate() :
               undefined;
             }
             if (scope.dateTypeSupported !== true) {
               scope.dateInput = scope.value;
               scope.dateDropDownInput = scope.value;
             }

             scope.setMode = function(mode) {
               scope.mode = mode;
             };

             var resetDateIfNeeded = function() {
               // Reset date if indeterminate position is now
               // or unknows.
               if (scope.withIndeterminatePosition &&
               (scope.indeterminatePosition === 'now' ||
                scope.indeterminatePosition === 'unknown')) {
                 scope.dateInput = '';
                 scope.date = '';
                 scope.year = '';
                 scope.month = '';
                 scope.time = '';
               }
             };

             // Build xml snippet based on input date.
             var buildDate = function() {
               var tag = scope.tagName !== undefined ?
               scope.tagName : 'gco:Date';
               var namespace = tag.split(':')[0];

               if (scope.dateTypeSupported !== true) {
                 // Check date against simple date pattern
                 // to add a css class to highlight error.
                 // Input will be saved anyway.
                 scope.isValidDate =
                 scope.dateInput.match(datePattern) !== null;

                 if (scope.dateInput === undefined) {
                   return;
                 } else {
                   tag = scope.tagName !== undefined ? scope.tagName :
                   (scope.dateInput.indexOf('T') === -1 ?
                    'gco:Date' : 'gco:DateTime');
                 }
                 scope.dateTime = scope.dateInput;
               } else if (scope.mode === 'year') {
                 scope.dateTime = scope.year;
               } else if (scope.mode === 'month') {
                 scope.dateTime = $filter('date')(scope.month, 'yyyy-MM');
               } else if (scope.time) {
                 tag = scope.tagName !== undefined ?
                 scope.tagName : 'gco:DateTime';
                 var time = $filter('date')(scope.time, 'HH:mm:ss');
                 // TODO: Set seconds, Timezone ?
                 scope.dateTime = $filter('date')(scope.date, 'yyyy-MM-dd');
                 scope.dateTime += 'T' + time;
               } else {
                 scope.dateTime = $filter('date')(scope.date, 'yyyy-MM-dd');
               }
               if (tag === '') {
                 scope.xmlSnippet = scope.dateTime;
               } else {
                 var attribute = '';
                 if (scope.withIndeterminatePosition &&
                 scope.indeterminatePosition !== '') {
                   attribute = ' indeterminatePosition="' +
                   scope.indeterminatePosition + '"';
                 }

                 if (scope.dateTime == null) {
                   scope.dateTime = '';
                 }

                 scope.xmlSnippet = '<' + tag +
                 ' xmlns:' +
                      namespace + '="' +
                      gnSchemaManagerService.findNamespaceUri(namespace,
                 gnCurrentEdit.schema) + '"' +
                 attribute + '>' +
                 scope.dateTime + '</' + tag + '>';
               }
             };

             scope.$watch('date', buildDate);
             scope.$watch('time', buildDate);
             scope.$watch('year', buildDate);
             scope.$watch('month', buildDate);
             scope.$watch('dateInput', buildDate);
             scope.$watch('indeterminatePosition', buildDate);
             scope.$watch('indeterminatePosition', resetDateIfNeeded);
             scope.$watch('xmlSnippet', function() {
               if (scope.id) {
                 // This is required on init to have the optionnaly
                 // templateFieldDirective initialized first so
                 // that the template is properly computed.
                 $timeout(function() {
                   $(scope.id).val(scope.xmlSnippet).change();
                 });
               }
             });


             buildDate();
           }
         };
       }]);
})();
