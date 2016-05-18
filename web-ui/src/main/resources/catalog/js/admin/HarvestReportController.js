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
  goog.provide('gn_harvest_report_controller');


  var module = angular.module('gn_harvest_report_controller',
      []);

  /**
   * GnHarvestReportController provides management interface
   * for report on harvesters.
   */
  module.controller('GnHarvestReportController', [
    '$scope',
    function($scope) {
      $scope.csvReport = function(event) {
        var json = [];
        var table = document.getElementById('harvestReport');
        var names = [];
        for (var i = 0, row; row = table.rows[i]; i++) {
          var obj = {};
          for (var j = 0, col; col = row.cells[j]; j++) {
            if (i == 0) {
              names[j] = col.innerText;
            } else {
              obj[names[j]] = col.innerText;
            }
          }
          if (i != 0) {
            json[i - 1] = obj;
          }
        }

        $scope.csvExport(json, event);
      };
    }]);
})();
