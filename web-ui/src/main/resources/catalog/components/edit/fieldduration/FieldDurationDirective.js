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
  goog.provide('gn_field_duration_directive');

  var module = angular.module('gn_field_duration_directive', []);

  /**
     *  Create a widget to handle editing of xsd:duration elements.
     *
     *  Format: PnYnMnDTnHnMnS
     *
     *  *  P indicates the period (required)
     *  * nY indicates the number of years
     *  * nM indicates the number of months
     *  * nD indicates the number of days
     *  * T indicates the start of a time section
     *       (required if you are going to specify hours, minutes, or seconds)
     *  * nH indicates the number of hours
     *  * nM indicates the number of minutes
     *  * nS indicates the number of seconds
     */
  module.directive('gnFieldDurationDiv', ['$http', '$rootScope',
    function($http, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          value: '@gnFieldDurationDiv',
          label: '@label',
          ref: '@ref',
          required: '@required'
        },
        templateUrl: '../../catalog/components/edit/fieldduration/partials/' +
            'fieldduration.html',
        link: function(scope, element, attrs) {

          var buildDuration = function() {
            var duration = [scope.sign === true ? '-' : '',
              'P',
              scope.years || 0, 'Y',
              scope.monthes || 0, 'M',
              scope.days || 0, 'D',
              'T',
              scope.hours || 0, 'H',
              scope.minutes || 0, 'M',
              scope.secondes || 0, 'S'];
            scope.value = duration.join('');
          };


          // Set the default value
          if (scope.value === undefined) {
            scope.value = 'P0Y0M0DT0H0M0S';
          }

          // Extract duration values
          var tokens = scope.value.split(/[PYMDTHMS]/);

          scope.sign = tokens[0] === '-';
          scope.years = parseInt(tokens[1]);
          scope.monthes = parseInt(tokens[2]);
          scope.days = parseInt(tokens[3]);
          scope.hours = parseInt(tokens[5]);
          scope.minutes = parseInt(tokens[6]);
          scope.secondes = parseInt(tokens[7]);
          scope.isDisabled = scope.ref == undefined;
          // Compute duration when any components change
          angular.forEach(['sign', 'years', 'monthes', 'days',
                           'hours', 'minutes', 'secondes'],
          function(value) {
            scope.$watch(value, buildDuration);
          });

        }
      };
    }]);
})();
