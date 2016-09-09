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
  goog.provide('gn_cat_controller');



  goog.require('gn_admin_menu');
  goog.require('gn_search_manager');
  goog.require('gn_session_service');


  var module = angular.module('gn_cat_controller',
      ['gn_search_manager', 'gn_session_service', 'gn_admin_menu']);


  module.constant('gnGlobalSettings', {
    proxyUrl: '../../proxy?url=',
    locale: {},
    isMapViewerEnabled: false,
    requireProxy: [],
    is3DModeAllowed: false,
    docUrl: 'http://geonetwork-opensource.org/manuals/trunk/',
    //docUrl: '../../doc/',
    modelOptions: {
      updateOn: 'default blur',
      debounce: {
        default: 300,
        blur: 0
      }
    },
    current: null
  });

  module.constant('gnLangs', {
    langs: {
      'eng': 'en',
      'dut': 'du',
      'fre': 'fr',
      'ger': 'ge',
      'kor': 'ko',
      'spa': 'es',
      'cze': 'cz',
      'cat': 'ca',
      'fin': 'fi'
    },
    getIso2Lang: function(iso3lang) {
      return this.langs[iso3lang];
    },
    getIso3Lang: function(iso2lang) {
      for (p in this.langs) {
        if (this.langs[p] == iso2lang) {
          return p;
        }
      }
    }
  });

  /**
   * The catalogue controller takes care of
   * loading site information, check user login state
   * and a facet search to get main site information.
   *
   * A body-level scope makes sense for example:
   *
   *  <body ng-controller="GnCatController">
   */
  module.controller('GnCatController', [
    '$scope', '$http', '$q', '$rootScope', '$translate',
    'gnSearchManagerService', 'gnConfigService', 'gnConfig',
    'gnGlobalSettings', '$location', 'gnUtilityService', 'gnSessionService',
    'gnLangs', 'gnAdminMenu',
    function($scope, $http, $q, $rootScope, $translate,
            gnSearchManagerService, gnConfigService, gnConfig,
            gnGlobalSettings, $location, gnUtilityService, gnSessionService,
            gnLangs, gnAdminMenu) {
      $scope.version = '0.0.1';
      //Display or not the admin menu
      if ($location.absUrl().indexOf('/admin.console') != -1) {
        $scope.viewMenuAdmin = true;
      }else {$scope.viewMenuAdmin = false}
      //Update Links for social media
      $scope.socialMediaLink = $location.absUrl();
      $scope.$on('$locationChangeSuccess', function(event) {
        $scope.socialMediaLink = $location.absUrl();
        $scope.showSocialMediaLink =
            $scope.socialMediaLink.includes('/metadata/');
      });
      // TODO : add language
      var tokens = location.href.split('/');
      $scope.service = tokens[6].split('?')[0];
      $scope.lang = tokens[5];
      gnLangs.current = $scope.lang;
      $scope.iso2lang = gnLangs.getIso2Lang(tokens[5]);
      $scope.nodeId = tokens[4];
      // TODO : get list from server side
      $scope.langs = gnLangs.langs;

      // Lang names to be displayed in language selector
      $scope.langLabels = {'eng': 'English', 'dut': 'Nederlands',
        'fre': 'Français', 'ger': 'Deutsch', 'kor': '한국의',
        'spa': 'Español', 'cat': 'Català', 'cze': 'Czech', 'fin': 'Suomeksi'};
      $scope.url = '';
      $scope.base = '../../catalog/';
      $scope.proxyUrl = gnGlobalSettings.proxyUrl;
      $scope.logoPath = '../../images/harvesting/';
      $scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
      $scope.isDebug = window.location.search.indexOf('debug') !== -1;

      $scope.pages = {
        home: 'home',
        signin: 'catalog.signin'
      };

      $scope.layout = {
        hideTopToolBar: false
      };

      /**
       * Number of selected metadata records.
       * Only one selection per session is allowed.
       */
      $scope.selectedRecordsCount = 0;

      /**
       * An ordered list of profiles
       */
      $scope.profiles = ['RegisteredUser', 'Editor',
                         'Reviewer', 'UserAdmin',
                         'Administrator'];
      $scope.info = {};
      $scope.user = {};
      $rootScope.user = $scope.user;
      $scope.authenticated = false;
      $scope.initialized = false;

      /**
       * Keep a reference on main cat scope
       * @return {*}
       */
      $scope.getCatScope = function() {return $scope};

      gnConfigService.load().then(function(c) {
        // Config loaded
        if (proj4 && angular.isArray(gnConfig['map.proj4js'])) {
          angular.forEach(gnConfig['map.proj4js'], function(item) {
            proj4.defs(item.code, item.value);
          });
        }
      });

      /**
       * Catalog facet summary providing
       * a global overview of the catalog content.
       */
      $scope.searchInfo = {};

      $scope.status = null;
      var defaultStatus = {
        title: '',
        link: '',
        msg: '',
        error: '',
        type: 'info',
        timeout: -1
      };

      $scope.loadCatalogInfo = function() {
        var promiseStart = $q.when('start');

        // Retrieve site information
        // TODO: Add INSPIRE, harvester, ... information
        var catInfo = promiseStart.then(function(value) {
          return $http.get('../api/site').
              success(function(data, status) {
                $scope.info = data;
                // Add the last time catalog info where updated.
                // That could be useful to append to catalog image URL
                // in order to trigger a reload of the logo when info are
                // reloaded.
                $scope.info['system/site/lastUpdate'] = new Date().getTime();
                $scope.initialized = true;
              }).
              error(function(data, status, headers, config) {
                $rootScope.$broadcast('StatusUpdated',
                   {
                     title: $translate('somethingWrong'),
                     msg: $translate('msgNoCatalogInfo'),
                     type: 'danger'});
              });
        });



        // Utility functions for user
        var userFn = {
          isAnonymous: function() {
            return angular.isUndefined(this);
          },
          isConnected: function() {
            return !this.isAnonymous();
          },
          canEditRecord: function(md) {
            if (!md || this.isAnonymous()) {
              return false;
            }

            // The md provide the information about
            // if the current user can edit records or not.
            var editable = angular.isDefined(md) &&
                angular.isDefined(md['geonet:info']) &&
                angular.isDefined(md['geonet:info'].edit) &&
                md['geonet:info'].edit == 'true';


            // A second filter is for harvested record
            // if the catalogue admin defined that those
            // records could be harvested.
            if (md.isHarvested === 'y') {
              return gnConfig['system.harvester.enableEditing'] === true &&
                  editable;
            }
            return editable;
          }
        };
        // Build is<ProfileName> and is<ProfileName>OrMore functions
        angular.forEach($scope.profiles, function(profile) {
          userFn['is' + profile] = function() {
            return profile === this.profile;
          };
          userFn['is' + profile + 'OrMore'] = function() {
            var profileIndex = $scope.profiles.indexOf(profile),
                allowedProfiles = [];
            angular.copy($scope.profiles, allowedProfiles);
            allowedProfiles.splice(0, profileIndex);
            return allowedProfiles.indexOf(this.profile) !== -1;
          };
        }
        );


        // Retrieve user information if catalog is online
        var userLogin = catInfo.then(function(value) {
          return $http.get('../api/me').
              success(function(me, status) {
                if (angular.isObject(me)) {
                  angular.extend($scope.user, me);
                  angular.extend($scope.user, userFn);
                  $scope.authenticated = true;
                } else {
                  $scope.authenticated = false;
                  $scope.user = undefined;
                }
              });
        });


        // Retrieve main search information
        var searchInfo = userLogin.then(function(value) {
          var url = 'qi?_content_type=json&summaryOnly=true';
          return gnSearchManagerService.search(url).
              then(function(data) {
                $scope.searchInfo = data;
              });
        });
      };
      $scope.adminMenu = gnAdminMenu.Administrator;
      $scope.$on('loadCatalogInfo', function(event, status) {
        $scope.loadCatalogInfo();
      });

      $scope.clearStatusMessage = function() {
        $scope.status = null;
        $('.gn-info').hide();
      };

      $scope.allowPublishInvalidMd = function() {
        return gnConfig['metadata.workflow.allowPublishInvalidMd'];
      };

      $scope.$on('StatusUpdated', function(event, status) {
        $scope.status = {};
        $.extend($scope.status, defaultStatus, status);
        $('.gn-info').show();
        // TODO : handle multiple messages
        if ($scope.status.timeout > 0) {
          setTimeout(function() {
            $scope.clearStatusMessage();
          }, $scope.status.timeout * 1000);
        }
      });

      gnSessionService.scheduleCheck($scope.user);
      $scope.session = gnSessionService.getSession();

      $scope.loadCatalogInfo();
    }]);

})();
