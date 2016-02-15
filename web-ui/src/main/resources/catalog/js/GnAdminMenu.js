(function() {
  goog.provide('gn_admin_menu');

  var module = angular.module('gn_admin_menu',[]);

  module.factory('gnAdminMenu',  function() {
    var userAdminMenu = [
        {name: 'harvesters', route: '#harvest',
          classes: 'btn-primary', icon: 'fa-cloud-download'},
        {name: 'statisticsAndStatus', route: '#dashboard',
          classes: 'btn-success', icon: 'fa-dashboard'},
        {name: 'reports', route: '#reports',
          classes: 'btn-success', icon: 'fa-file-text-o'},
        {name: 'usersAndGroups', route: '#organization',
          classes: 'btn-default', icon: 'fa-group'}
      ];
      var menu = {
        UserAdmin: userAdminMenu,
        Administrator: [
          // TODO : create gn classes
          {name: 'metadatasAndTemplates', route: '#metadata',
            classes: 'btn-primary', icon: 'fa-archive'},
          {name: 'harvesters', route: '#harvest', //url: 'harvesting',
            classes: 'btn-primary', icon: 'fa-cloud-download'},
          {name: 'statisticsAndStatus', route: '#dashboard',
            classes: 'btn-success', icon: 'fa-dashboard'},
          {name: 'reports', route: '#reports',
            classes: 'btn-success', icon: 'fa-file-text-o'},
          {name: 'classificationSystems', route: '#classification',
            classes: 'btn-info', icon: 'fa-tags'},
          {name: 'standards', route: '#standards',
            classes: 'btn-info', icon: 'fa-puzzle-piece'},
          {name: 'usersAndGroups', route: '#organization',
            classes: 'btn-default', icon: 'fa-group'},
          {name: 'settings', route: '#settings',
            classes: 'btn-warning', icon: 'fa-gear'},
          {name: 'tools', route: '#tools',
            classes: 'btn-warning', icon: 'fa-medkit'}]
        // TODO : add other role menu
      };

    return menu
  });

})();