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
  goog.provide('gn_admin_controller');

  goog.require('gn_admin_menu');
  goog.require('gn_adminmetadata_controller');
  goog.require('gn_admintools_controller');
  goog.require('gn_cat_controller');
  goog.require('gn_classification_controller');
  goog.require('gn_dashboard_controller');
  goog.require('gn_harvest_controller');
  goog.require('gn_report_controller');
  goog.require('gn_settings_controller');
  goog.require('gn_usergroup_controller');

  var module = angular.module('gn_admin_controller',
      ['gn_dashboard_controller', 'gn_usergroup_controller',
       'gn_admintools_controller', 'gn_settings_controller',
       'gn_adminmetadata_controller', 'gn_classification_controller',
       'gn_harvest_controller', 'gn_report_controller', 'gn_admin_menu']);


  var tplFolder = '../../catalog/templates/admin/';

  module.provider('authorizationService', [function() {

    this.$get = [function() {
      return {
        check: function(rol) {

          //FIXME get a better way to get the authenticated user
          //other UIs may not have this? This is dirty
          this.scope = angular.element(
              $('*[data-ng-controller=GnCatController]')[0])
                              .scope();
          if (this.scope.routelistener) {
            this.scope.routelistener();
          }

          this.listener = this.scope.$watch('user.profile',
                                            function(newProfile, oldProfile) {


                if (!newProfile) {
                  return;
                }

                var roles = ['GUEST', 'REGISTEREDUSER', 'EDITOR',
                  'REVIEWER', 'USERADMIN', 'ADMINISTRATOR'];

                var irol = 0;
                for (i = 0; i < roles.length; i++) {
                  if (rol.toUpperCase() == roles[i].toUpperCase()) {
                    irol = i;
                  }
                }

                var iprofile = 0;
                for (i = 0; i < roles.length; i++) {
                  if (newProfile.toUpperCase() == roles[i].toUpperCase()) {
                    iprofile = i;
                  }
                }

                if (iprofile < irol) {
                  var href = window.location.href;
                  if (href.indexOf('#') > 0) {
                    href = href.substring(0, href.indexOf('#'));
                  }

                  //redirect to home
                  window.location.href = (href.substring(0,
                      href.lastIndexOf('/')) + '/catalog.search');
                }
              });

          this.scope.routelistener = this.listener;
        }
      };
    }];
  }]);

  module.config(['$routeProvider', 'authorizationServiceProvider',
    function($routeProvider, authorizationService) {
      $routeProvider.
          when('/metadata', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminMetadataController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Guest');
              }
            }
          }).
          when('/metadata/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminMetadataController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Guest');
              }
            }
          }).
          when('/metadata/schematron/:schemaName', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminMetadataController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Guest');
              }
            }
          }).
          when('/metadata/schematron/:schemaName/:schematronId', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminMetadataController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Guest');
              }
            }
          }).
          when('/metadata/:tab/:metadataAction/:schema', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminMetadataController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Guest');
              }
            }
          }).
          when('/dashboard', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnDashboardController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/dashboard/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnDashboardController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/organization', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnUserGroupController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/organization/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnUserGroupController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/organization/:tab/:userOrGroup', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnUserGroupController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('RegisteredUser');
              }
            }
          }).
          when('/classification', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnClassificationController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/classification/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnClassificationController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/tools', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminToolsController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Administrator');
              }
            }
          }).
          when('/tools/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminToolsController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Administrator');
              }
            }
          }).
          when('/tools/:tab/select/:selectAll/process/:processId', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnAdminToolsController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Administrator');
              }
            }
          }).
          when('/harvest', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnHarvestController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/harvest/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnHarvestController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('Editor');
              }
            }
          }).
          when('/settings', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnSettingsController',
            reloadOnSearch: false,
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('UserAdmin');
              }
            }
          }).
          when('/settings/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnSettingsController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('UserAdmin');
              }
            }
          }).
          when('/reports', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnReportController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('UserAdmin');
              }
            }
          }).
          when('/reports/:tab', {
            templateUrl: tplFolder + 'page-layout.html',
            controller: 'GnReportController',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('UserAdmin');
              }
            }
          }).
          when('/home', {
            templateUrl: tplFolder + 'admin.html',
            resolve: {
              permission: function() {
                authorizationService.$get[0]().check('UserAdmin');
              }
            }
          }).
          otherwise({
            redirectTo: '/home'
          });
    }]);

  /**
   * The admin console controller.
   *
   * Example:
   *
   *     <body ng-controller="GnAdminController">
   */
  module.controller('GnAdminController', [
    '$scope', '$http', '$q', '$rootScope', '$route', '$routeParams',
    'gnUtilityService', 'gnAdminMenu',
    function($scope, $http, $q, $rootScope, $route, $routeParams,
        gnUtilityService, gnAdminMenu) {
      $scope.menu = gnAdminMenu;
      /**
       * Define menu position on the left (nav-stacked)
       * or on top of the page.
       */
      $scope.navStacked = true;

      $scope.getTpl = function(pageMenu) {
        $scope.type = pageMenu.defaultTab;
        $.each(pageMenu.tabs, function(index, value) {
          var isMatch = false;

          if (angular.isUndefined($routeParams.dashboard)) {
            // If no $routeParams.tab, check if the option is the default one,
            // otherwise  compare the option with $routeParams.tab
            isMatch = ($routeParams.tab === undefined &&
              value.type === pageMenu.defaultTab) ||
              (value.type === $routeParams.tab);
          } else {
            isMatch = value.href.indexOf(
              encodeURIComponent($routeParams.dashboard)) !== -1;
          }

          if (isMatch) {
            $scope.type = ($routeParams.tab !== undefined)?$routeParams.tab:pageMenu.defaultTab;
            $scope.href = value.href;
          }
        });
        //do not try to load undefined.html
        if (!pageMenu || !pageMenu.folder || !$scope.type) return '';
        return tplFolder + pageMenu.folder + $scope.type + '.html';
      };

      $scope.csvExport = function(json, e) {
        $(e.target).next('pre.gn-csv-export').remove();
        $(e.target).after("<pre class='gn-csv-export'>" +
                gnUtilityService.toCsv(json) + '</pre>');
      };

      /**
       * Return menu Angular route or GeoNetwork legacy URLs
       * according to $scope.menu configuration.
       */
      $scope.getMenuUrl = function(menu) {
        if (menu.route) {
          return menu.route;
        } else if (menu.url) {
          return $scope.url + menu.url;
        }
      };

      /**
       * Return menu according to user profile
       */
      $scope.getMenu = function() {
        return $scope.menu[$scope.user.profile];
      };
    }]);

})();
