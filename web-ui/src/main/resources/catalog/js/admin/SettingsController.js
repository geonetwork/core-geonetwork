(function() {
  goog.provide('gn_settings_controller');

  goog.require('gn_csw_settings_controller');
  goog.require('gn_csw_test_controller');
  goog.require('gn_csw_virtual_controller');
  goog.require('gn_logo_settings_controller');
  goog.require('gn_mapserver_controller');
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
       'gn_scroll_spy']
      );

  module.controller('GnSettingsController', ['$scope',
    function($scope) {

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
              href: '#/settings/csw'
            },{
              type: 'csw-virtual',
              label: 'manageVirtualCSW',
              href: '#/settings/csw-virtual'
            },{
              type: 'csw-test',
              label: 'testCSW',
              href: '#/settings/csw-test'
            },{
              type: 'mapservers',
              icon: 'fa-globe',
              label: 'manageMapServers',
              href: '#/settings/mapservers'
            }]};
    }]);
})();
