(function() {
  goog.provide('gn_admin_controller');
  goog.require('gn_adminmetadata_controller');
  goog.require('gn_admintools_controller');
  goog.require('gn_cat_controller');
  goog.require('gn_classification_controller');
  goog.require('gn_dashboard_controller');
  goog.require('gn_harvest_controller');
  goog.require('gn_report_controller');
  goog.require('gn_settings_controller');
  goog.require('gn_standards_controller');
  goog.require('gn_usergroup_controller');

  var module = angular.module('gn_admin_controller',
      ['gn_dashboard_controller', 'gn_usergroup_controller',
       'gn_admintools_controller', 'gn_settings_controller',
       'gn_adminmetadata_controller', 'gn_classification_controller',
       'gn_harvest_controller', 'gn_standards_controller',
       'gn_report_controller']);


  var tplFolder = '../../catalog/templates/admin/';
  
  module.service('authorizationService', ['$location', function($location) {     
      this.location = $location;
      this.check = function(route, rol) {
        if(this.listener) {
          this.listener();
        }

        //FIXME get a better way to get the authenticated user
        //other UIs may not have this? This is dirty
        this.scope = angular.element($("*[data-ng-controller=GnCatController]")[0])
                          .scope();
        
        this.scope["$location"] = this.$location;
        
        var $location = this.$location;
        
        this.listener = this.scope.$watch('user.profile', function(newProfile, oldProfile) {
          
          
          if(!newProfile) {
            return;
          }

          var roles = ["GUEST", "REGISTEREDUSER", "EDITOR", 
                  "REVIEWER", "USERADMIN", "ADMINISTRATOR"];
          
          var irol = 0;
          for(i = 0; i < roles.length; i++) {
            if(rol.toUpperCase() == roles[i].toUpperCase()) {
              irol = i;
            }
          }
          
          var iprofile = 0;
          for(i = 0; i < roles.length; i++) {
            if(newProfile.toUpperCase() == roles[i].toUpperCase()) {
              iprofile = i;
            }
          }
          
          if(iprofile < irol) {
            var href = window.location.href;
            if(href.indexOf("#") > 0) {
              href = href.substring(0, href.indexOf("#"));
            }
            
            //redirect to home
            window.location.href = (href.substring(0, 
                href.lastIndexOf("/")) + "/catalog.search");
          }
        });
      }
  }]);
  
  module.config(['$routeProvider',
    function($routeProvider) {
     $routeProvider.
        when('/metadata', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Guest');
            }
          }
        }).
        when('/metadata/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Guest');
            }
          }
          }).
        when('/metadata/schematron/:schemaName', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Guest');
            }
          }
        }).
        when('/metadata/schematron/:schemaName/:schematronId', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Guest');
            }
          }
        }).
        when('/metadata/:tab/:metadataAction/:schema', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController',
          resolve: {
            permission: function(authorizationService, $route) {
               authorizationService.check($route, 'Guest');
            }
          }
        }).
        when('/dashboard', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnDashboardController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/dashboard/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnDashboardController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/organization', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/organization/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/organization/:tab/:userOrGroup', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'RegisteredUser');
            }
          }
        }).
        when('/classification', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnClassificationController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/classification/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnClassificationController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/tools', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/tools/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/tools/:tab/select/:selectAll/process/:processId', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/harvest', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnHarvestController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/harvest/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnHarvestController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/settings', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnSettingsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/settings/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnSettingsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/standards', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnStandardsController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Editor');
            }
          }
        }).
        when('/reports', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnReportController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        when('/reports/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnReportController',
          resolve: {
            permission: function(authorizationService, $route) {
              authorizationService.check($route, 'Administrator');
            }
          }
        }).
        otherwise({templateUrl: tplFolder + 'admin.html'});
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
    'gnUtilityService',
    function($scope, $http, $q, $rootScope, $route, $routeParams,
        gnUtilityService) {
      /**
       * Define admin console menu for each type of user
       */
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
      $scope.menu = {
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

      /**
       * Define menu position on the left (nav-stacked)
       * or on top of the page.
       */
      $scope.navStacked = true;

      $scope.getTpl = function(pageMenu) {
        $scope.type = pageMenu.defaultTab;
        $.each(pageMenu.tabs, function(index, value) {
          if (value.type === $routeParams.tab) {
            $scope.type = $routeParams.tab;
          }
        });
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
