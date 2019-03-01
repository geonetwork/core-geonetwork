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
  goog.provide('gn_alert');

  var module = angular.module('gn_alert', ['ngSanitize']);

  module.value('gnAlertValue', []);

  module.service('gnAlertService', [
    'gnAlertValue',
    '$timeout',
    function(gnAlertValue, $timeout) {

      // delay to close the alert in milliseconds.
      var delay = 2000;

      /**
       * Adds an alert to be handled by the alert manager.
       *
       * @param {Object} alert Alert to display.
       * @param {Number} d     Timeout to close the alert (in seconds)
       */
      this.addAlert = function(alert, d) {
        var alertToUpdatePos = -1;

        if (alert.id) {
          // Check if exists an alert with same id and replace it
          for (var i = 0; i < gnAlertValue.length; i++) {
            var selectedAlertId = gnAlertValue[i].id;

            if (selectedAlertId && (alert.id == selectedAlertId)) {
              alertToUpdatePos = i;
              break;
            }
          }
        }

        if (alertToUpdatePos > -1) {
          gnAlertValue[alertToUpdatePos] = alert;
        } else {
          gnAlertValue.push(alert);

          // Error alerts require to be closed by the user
          if (alert.type !== 'danger') {
            $timeout(function() {
              gnAlertValue.splice(0, 1);
            }, (d * 1000) ||  delay);
          }
        }
      };
    }]);

  module.directive('gnAlertManager', [
    'gnAlertValue',
    function(gnAlertValue) {
      return {
        replace: true,
        restrict: 'A',
        templateUrl: '../../catalog/components/common/alert/' +
            'partials/alert.html',
        link: function(scope, element, attrs) {
          scope.alerts = gnAlertValue;

          scope.closeAlert = function(pos) {
            if ((pos > -1) &&
                (pos < gnAlertValue.length)) {
              gnAlertValue.splice(pos, 1);
            }
          };
        }
      };
    }]);
})();
