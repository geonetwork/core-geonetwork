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
  goog.provide('gn_admin_menu');

  var module = angular.module('gn_admin_menu', []);

  module.factory('gnAdminMenu', function() {
    var userAdminMenu = [
      {name: 'usersAndGroups', route: '#organization',
        classes: 'btn-default', icon: 'fa-group'},
      {name: 'harvesters', route: '#harvest',
        classes: 'btn-primary', icon: 'fa-cloud-download'},
      {name: 'statisticsAndStatus', route: '#dashboard',
        classes: 'btn-success', icon: 'fa-dashboard'},
      {name: 'reports', route: '#reports',
        classes: 'btn-success', icon: 'fa-file-text-o'},
      {name: 'settings', route: '#settings',
        classes: 'btn-warning', icon: 'fa-gear'}
    ];
    var menu = {
      UserAdmin: userAdminMenu,
      Administrator: [
        // TODO : create gn classes
        {name: 'metadatasAndTemplates', route: '#metadata',
          classes: 'btn-primary', icon: 'fa-archive'},
        {name: 'usersAndGroups', route: '#organization',
          classes: 'btn-default', icon: 'fa-group'},
        {name: 'harvesters', route: '#harvest', //url: 'harvesting',
          classes: 'btn-primary', icon: 'fa-cloud-download'},
        {name: 'statisticsAndStatus', route: '#dashboard',
          classes: 'btn-success', icon: 'fa-dashboard'},
        {name: 'reports', route: '#reports',
          classes: 'btn-success', icon: 'fa-file-text-o'},
        {name: 'classificationSystems', route: '#classification',
          classes: 'btn-info', icon: 'fa-tags'},
        {name: 'settings', route: '#settings',
          classes: 'btn-warning', icon: 'fa-gear'},
        {name: 'tools', route: '#tools',
          classes: 'btn-warning', icon: 'fa-medkit'}]
      // TODO : add other role menu
    };

    return menu;
  });

})();
