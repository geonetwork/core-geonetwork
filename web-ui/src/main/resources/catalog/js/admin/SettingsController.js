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
  goog.provide('gn_settings_controller');











  goog.require('gn_cssstyle_settings_controller');
  goog.require('gn_csw_settings_controller');
  goog.require('gn_csw_test_controller');
  goog.require('gn_csw_virtual_controller');
  goog.require('gn_logo_settings_controller');
  goog.require('gn_mapserver_controller');
  goog.require('gn_metadata_identifier_templates_controller');
  goog.require('gn_scroll_spy');
  goog.require('gn_sources_controller');
  goog.require('gn_system_settings_controller');

  var module = angular.module('gn_settings_controller',
      ['gn_system_settings_controller',
       'gn_csw_settings_controller',
       'gn_csw_virtual_controller',
       'gn_mapserver_controller',
       'gn_csw_test_controller',
       'gn_logo_settings_controller',
       'gn_sources_controller',
       'gn_metadata_identifier_templates_controller',
       'gn_cssstyle_settings_controller',
       'gn_scroll_spy']
      );

  module.controller('GnSettingsController', ['$scope',
    function($scope) {

      var userAdminTabs = [{
        type: 'sources',
        icon: 'fa-database',
        label: 'manageSources',
        href: '#/settings/sources'
      }, {
        type: 'ui',
        label: 'ui',
        icon: 'fa-puzzle-piece',
        href: '#/settings/ui'
      }];

      $scope.pageMenu = {
        folder: 'settings/',
        defaultTab: 'system',
        tabs:
            [{
              type: 'system',
              label: 'settings',
              icon: 'fa-cogs',
              href: '#/settings/system'
            },{
              type: 'ui',
              label: 'ui',
              icon: 'fa-puzzle-piece',
              href: '#/settings/ui'
            },{
              type: 'cssstyle',
              label: 'cssstyle',
              icon: 'fa-camera',
              href: '#/settings/cssstyle'
            },{
              type: 'logo',
              label: 'manageLogo',
              icon: 'fa-picture-o',
              href: '#/settings/logo'
            },{
              type: 'sources',
              icon: 'fa-database',
              label: 'manageSources',
              href: '#/settings/sources'
            },{
              type: 'csw',
              label: 'manageCSW',
              icon: 'fa-server',
              href: '#/settings/csw'
            },{
              type: 'csw-virtual',
              label: 'manageVirtualCSW',
              icon: 'fa-server',
              href: '#/settings/csw-virtual'
            },{
              type: 'csw-test',
              label: 'testCSW',
              icon: 'fa-server',
              href: '#/settings/csw-test'
            },{
              type: 'mapservers',
              icon: 'fa-globe',
              label: 'manageMapServers',
              href: '#/settings/mapservers'
            }]};


      function loadConditionalTabs() {
        if ($scope.user.profile === 'UserAdmin') {
          $scope.pageMenu.tabs = userAdminTabs;
          $scope.pageMenu.defaultTab = 'sources';
        }
      }

      loadConditionalTabs();

      $scope.$watchCollection('user', function (n, o) {
        if (n !== o) {
          loadConditionalTabs();
        }
      });

    }]);
})();
