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
  goog.provide('gn_dashboard_controller');



  goog.require('gn_dashboard_content_stat_controller');
  goog.require('gn_dashboard_search_stat_controller');
  goog.require('gn_dashboard_status_controller');
  goog.require('gn_vcs_controller');

  var module = angular.module('gn_dashboard_controller',
      ['gn_dashboard_status_controller',
       'gn_dashboard_search_stat_controller',
       'gn_dashboard_content_stat_controller',
       'gn_vcs_controller']);


  /**
   *
   */
  module.controller('GnDashboardController', ['$scope', '$http',
    function($scope, $http) {


      $scope.pageMenu = {
        folder: 'dashboard/',
        defaultTab: 'status',
        tabs:
            [{
              type: 'status',
              label: 'status',
              icon: 'fa-dashboard',
              href: '#/dashboard/status'
            },{
              type: 'statistics-search',
              label: 'searchStatistics',
              icon: 'fa-search',
              href: '#/dashboard/statistics-search'
            },{
              type: 'statistics-content',
              label: 'contentStatistics',
              icon: 'fa-bar-chart',
              href: '#/dashboard/statistics-content'
            },{
              type: 'information',
              label: 'information',
              icon: 'fa-list-ul',
              href: '#/dashboard/information'
            },{
              type: 'versioning',
              label: 'versioning',
              icon: 'fa-rss',
              href: '#/dashboard/versioning'
            }]
      };

      $scope.info = {};

      $http.get('../api/site/info').
          success(function(data) {
            $scope.info = data;
          });

    }]);

})();
