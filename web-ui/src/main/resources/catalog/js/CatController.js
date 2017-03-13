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
  goog.require('gn_saved_selections');
  goog.require('gn_search_manager');
  goog.require('gn_session_service');


  var module = angular.module('gn_cat_controller',
      ['gn_search_manager', 'gn_session_service',
        'gn_admin_menu', 'gn_saved_selections']);


  module.constant('gnSearchSettings', {});
  module.constant('gnViewerSettings', {});
  module.constant('gnGlobalSettings', function() {
    var defaultConfig = {
      'langDetector': {
        'fromHtmlTag': false,
        'regexp': '^\/[a-zA-Z0-9_\-]+\/[a-zA-Z0-9_\-]+\/([a-z]{3})\/',
        'default': 'eng'
      },
      'nodeDetector': {
        'regexp': '^\/[a-zA-Z0-9_\-]+\/([a-zA-Z0-9_\-]+)\/[a-z]{3}\/',
        'default': 'srv'
      },
      'mods': {
        'header': {
          'enabled': true,
          'languages': {
            'eng': 'en',
            'dut': 'du',
            'fre': 'fr',
            'ger': 'ge',
            'kor': 'ko',
            'spa': 'es',
            'cze': 'cz',
            'cat': 'ca',
            'fin': 'fi',
            'ice': 'is'
          }
        },
        'home': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.search#/home'
        },
        'search': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.search#/search',
          'hitsperpageValues': [10, 50, 100],
          'paginationInfo': {
            'hitsPerPage': 20
          },
          'facetsSummaryType': 'details',
          'facetTabField': '',
          'facetConfig': [
            // {
            // key: 'createDateYear',
            // labels: {
            //   eng: 'Published',
            //   fre: 'Publication'
            // }}
          ],
          'filters': {},
          'sortbyValues': [{
            'sortBy': 'relevance',
            'sortOrder': ''
          }, {
            'sortBy': 'changeDate',
            'sortOrder': ''
          }, {
            'sortBy': 'title',
            'sortOrder': 'reverse'
          }, {
            'sortBy': 'rating',
            'sortOrder': ''
          }, {
            'sortBy': 'popularity',
            'sortOrder': ''
          }, {
            'sortBy': 'denominatorDesc',
            'sortOrder': ''
          }, {
            'sortBy': 'denominatorAsc',
            'sortOrder': 'reverse'
          }],
          'sortBy': 'relevance',
          'resultViewTpls': [{
            'tplUrl': '../../catalog/components/' +
                'search/resultsview/partials/viewtemplates/grid.html',
            'tooltip': 'Grid',
            'icon': 'fa-th'
          }],
          'resultTemplate': '../../catalog/components/' +
              'search/resultsview/partials/viewtemplates/grid.html',
          'formatter': {
            'list': [{
              'label': 'full',
              'url' : '../api/records/{{md.getUuid()}}/' +
                  'formatters/xsl-view?root=div&view=advanced'
            }]
          },
          'linkTypes': {
            'links': ['LINK', 'kml'],
            'downloads': ['DOWNLOAD'],
            'layers': ['OGC'],
            'maps': ['ows']
          }
        },
        'map': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.search#/map',
          'is3DModeAllowed': true,
          'isSaveMapInCatalogAllowed': true,
          'bingKey':
              'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D',
          'storage': 'sessionStorage',
          'map': '../../map/config-viewer.xml',
          'listOfServices': {
            'wms': [],
            'wmts': []
          },
          'useOSM': true,
          'context': '',
          'layer': {
            'url': 'http://www2.demis.nl/mapserver/wms.asp?',
            'layers': 'Countries',
            'version': '1.1.1'
          },
          'projection': 'EPSG:3857',
          'projectionList': [{
            'code': 'EPSG:4326',
            'label': 'WGS84 (EPSG:4326)'
          }, {
            'code': 'EPSG:3857',
            'label': 'Google mercator (EPSG:3857)'
          }]
        },
        'editor': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.edit'
        },
        'admin': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/admin.console'
        },
        'signin': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.signin'
        },
        'signout': {
          'appUrl': '../../signout'
        }
      }
    };

    return {
      proxyUrl: '',
      locale: {},
      isMapViewerEnabled: false,
      requireProxy: [],
      gnCfg: angular.copy(defaultConfig),
      gnUrl: '',
      docUrl: 'http://geonetwork-opensource.org/manuals/trunk/',
      //docUrl: '../../doc/',
      modelOptions: {
        updateOn: 'default blur',
        debounce: {
          default: 300,
          blur: 0
        }
      },
      current: null,
      init: function(config, gnUrl, gnViewerSettings, gnSearchSettings) {
        // Remap some old settings with new one
        angular.extend(this.gnCfg, config || {});
        this.gnUrl = gnUrl || '../';
        this.proxyUrl = this.gnUrl + '../proxy?url=';
        gnViewerSettings.mapConfig = this.gnCfg.mods.map;
        angular.extend(gnSearchSettings, this.gnCfg.mods.search);
        this.isMapViewerEnabled = this.gnCfg.mods.map.enabled;
        gnViewerSettings.bingKey = this.gnCfg.mods.map.bingKey;
        gnViewerSettings.owsContext = this.gnCfg.mods.map.context;
        gnViewerSettings.wmsUrl = this.gnCfg.mods.map.layer.url;
        gnViewerSettings.layerName = this.gnCfg.mods.map.layer.name;
      },
      getDefaultConfig: function() {
        return angulaWr.copy(defaultConfig);
      }
    };
  }());

  module.constant('gnLangs', {
    langs: {},
    current: null,
    detectLang: function(detector, gnGlobalSettings) {
      // If already detected
      if (gnGlobalSettings.iso3lang) {
        return gnGlobalSettings.iso3lang;
      }

      var iso2lang, iso3lang;

      // Init language list
      this.langs =
          gnGlobalSettings.gnCfg.mods.header.languages;

      // Detect language from HTML lang tag, regex on URL
      if (detector) {
        if (detector.fromHtmlTag) {
          iso2lang = $('html').attr('lang').substr(0, 2);
        } else if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            var urlLang = res[1];
            if (this.isValidIso2Lang(urlLang)) {
              iso2lang = urlLang;
            } else if (this.isValidIso3Lang(urlLang)) {
              iso2lang = this.getIso2Lang(urlLang);
            } else {
              console.warn('URL lang \'' + urlLang +
                  '\' is not a valid language code.');
            }
          }
        } else if (detector.default) {
          iso2lang = detector.default;
        }
        iso3lang = this.getIso3Lang(iso2lang || detector.default);
      }
      this.current = iso3lang || 'eng';

      // Set locale to global settings. This is
      // used by locale loader.
      gnGlobalSettings.iso3lang = this.current;
      gnGlobalSettings.lang = this.getIso2Lang(this.current);
      gnGlobalSettings.locale = {
        iso3lang: this.current
      };
      return this.current;
    },
    getCurrent: function() {
      return this.current;
    },
    isValidIso3Lang: function(lang) {
      return angular.isDefined(this.langs[lang]);
    },
    isValidIso2Lang: function(lang) {
      for (p in this.langs) {
        if (this.langs[p] == lang) {
          return true;
        }
      }
      return false;
    },
    getIso2Lang: function(iso3lang) {
      return this.langs[iso3lang] || 'en';
    },
    getIso3Lang: function(iso2lang) {
      for (p in this.langs) {
        if (this.langs[p] == iso2lang) {
          return p;
        }
      }
      return 'eng';
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
    'gnLangs', 'gnAdminMenu', 'gnViewerSettings', 'gnSearchSettings',
    function($scope, $http, $q, $rootScope, $translate,
            gnSearchManagerService, gnConfigService, gnConfig,
            gnGlobalSettings, $location, gnUtilityService, gnSessionService,
            gnLangs, gnAdminMenu, gnViewerSettings, gnSearchSettings) {
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
            ($scope.socialMediaLink.indexOf('/metadata/') != -1);
      });
      $scope.getPermalink = gnUtilityService.getPermalink;

      try {
        var tokens = location.href.split('/');
        $scope.service = tokens[6].split('?')[0];
      } catch(e) {
        // console.log("Failed to extract current service from URL.");
      }

      // If gnLangs current already set by config, do not use URL
      $scope.langs = gnGlobalSettings.gnCfg.mods.header.languages;
      $scope.lang = gnLangs.detectLang(null, gnGlobalSettings);
      $scope.iso2lang = gnLangs.getIso2Lang($scope.lang);

      function detectNode(detector) {
        if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            return res[1];
          }
        }
        return detector.default || 'srv';
      }
      $scope.nodeId = detectNode(gnGlobalSettings.gnCfg.nodeDetector);

      // Lang names to be displayed in language selector
      $scope.langLabels = {'eng': 'English', 'dut': 'Nederlands',
        'fre': 'Français', 'ger': 'Deutsch', 'kor': '한국의',
        'spa': 'Español', 'cat': 'Català', 'cze': 'Czech',
        'fin': 'Suomeksi', 'fin': 'Suomeksi', 'ice': 'Íslenska'};
      $scope.url = '';
      $scope.gnUrl = gnGlobalSettings.gnUrl;
      $scope.gnCfg = gnGlobalSettings.gnCfg;
      $scope.proxyUrl = gnGlobalSettings.proxyUrl;
      $scope.logoPath = gnGlobalSettings.gnUrl + '../images/harvesting/';
      $scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
      $scope.isDebug = window.location.search.indexOf('debug') !== -1;


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
                     title: $translate.instant('somethingWrong'),
                     msg: $translate.instant('msgNoCatalogInfo'),
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
          angular.forEach(gnGlobalSettings.gnCfg.mods.search.filters,
              function(v, k) {
                url += '&' + k + '=' + v;
              });
          return gnSearchManagerService.search(url).
              then(function(data) {
                $scope.searchInfo = data;
              });
        });
      };
      $scope.userAdminMenu = gnAdminMenu.UserAdmin;
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
