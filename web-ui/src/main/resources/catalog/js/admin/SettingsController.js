(function() {
  geonet.provide('gn_settings_controller');









  geonet.require('gn_csw_settings_controller');
  geonet.require('gn_csw_test_controller');
  geonet.require('gn_csw_virtual_controller');
  geonet.require('gn_logo_settings_controller');
  geonet.require('gn_scroll_spy');
  geonet.require('gn_system_settings_controller');

  var module = angular.module('gn_settings_controller',
      ['gn_system_settings_controller',
       'gn_csw_settings_controller',
       'gn_csw_virtual_controller',
       'gn_csw_test_controller',
       'gn_logo_settings_controller',
       'gn_scroll_spy']);


  /**
   *
   */
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
            }]
      };
    }]);

})();
