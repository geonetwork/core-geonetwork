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
            'dut': 'nl',
            'fre': 'fr',
            'ger': 'ge',
            'kor': 'ko',
            'spa': 'es',
            'cze': 'cz',
            'cat': 'ca',
            'fin': 'fi',
            'ice': 'is',
            'ita' : 'it',
            'rus': 'ru',
            'chi': 'zh'
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
              'url' : '../api/records/{{uuid}}/' +
                  'formatters/xsl-view?root=div&view=advanced'
            }]
          },
          'grid': {
            'related': ['parent', 'children', 'services', 'datasets']
          },
          'linkTypes': {
            'links': ['LINK', 'kml'],
            'downloads': ['DOWNLOAD'],
            'layers': ['OGC'],
            'maps': ['ows']
          },
          'isFilterTagsDisplayedInSearch': false
        },
        'map': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.search#/map',
          'is3DModeAllowed': true,
          'isSaveMapInCatalogAllowed': true,
          'isExportMapAsImageEnabled': false,
          'storage': 'sessionStorage',
          'listOfServices': {
            'wms': [],
            'wmts': []
          },
          'projection': 'EPSG:3857',
          'projectionList': [{
            'code': 'EPSG:4326',
            'label': 'WGS84 (EPSG:4326)'
          }, {
            'code': 'EPSG:3857',
            'label': 'Google mercator (EPSG:3857)'
          }],
          'disabledTools': {
            'processes': false,
            'addLayers': false,
            'layers': false,
            'filter': false,
            'contexts': false,
            'print': false,
            'mInteraction': false,
            'graticule': false,
            'syncAllLayers': false,
            'drawVector': false
          },
          'graticuleOgcService': {},
          'map-viewer': {
            'context': '../../map/config-viewer.xml',
            'extent': [0, 0, 0, 0],
            'layers': []
          },
          'map-search': {
            'context': '../../map/config-viewer.xml',
            'extent': [0, 0, 0, 0],
            'layers': []
          },
          'map-editor': {
            'context': '',
            'extent': [0, 0, 0, 0],
            'layers': [{'type': 'osm'}]
          }
        },
        'geocoder': 'https://secure.geonames.org/searchJSON',
        'editor': {
          'enabled': true,
          'appUrl': '../../srv/{{lang}}/catalog.edit',
          'isUserRecordsOnly': false,
          'isFilterTagsDisplayed': false,
          'createPageTpl':
              '../../catalog/templates/editor/new-metadata-horizontal.html'
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
      docUrl: 'http://geonetwork-opensource.org/manuals/3.4.x/',
      //docUrl: '../../doc/',
      modelOptions: {
        updateOn: 'default blur',
        debounce: {
          default: 300,
          blur: 0
        }
      },
      current: null,
      shibbolethEnabled: false,
      init: function(config, gnUrl, gnViewerSettings, gnSearchSettings) {
        // start from the default config to make sure every field is present
        // and override with config arg if required
        angular.merge(this.gnCfg, config, {});

        // secial case: languages (replace with object from config if available)
        this.gnCfg.mods.header.languages = angular.extend({
          mods: {
            header: {
              languages: {}
            }
          }
        }, config).mods.header.languages;

        this.gnUrl = gnUrl || '../';
        this.proxyUrl = this.gnUrl + '../proxy?url=';
        gnViewerSettings.mapConfig = this.gnCfg.mods.map;
        angular.extend(gnSearchSettings, this.gnCfg.mods.search);
        this.isMapViewerEnabled = this.gnCfg.mods.map.enabled;
        gnViewerSettings.bingKey = this.gnCfg.mods.map.bingKey;
        gnViewerSettings.defaultContext =
          gnViewerSettings.mapConfig['map-viewer'].context;
        gnViewerSettings.geocoder = this.gnCfg.mods.geocoder;
      },
      getDefaultConfig: function() {
        return angular.copy(defaultConfig);
      },
      // this returns a copy of the default config without the languages object
      // this way, the object can be used as reference for a complete ui
      // settings page
      getMergeableDefaultConfig: function() {
        var copy = angular.copy(defaultConfig);
        copy.mods.header.languages = {};
        copy.mods.search.grid.related = [];
        return copy;
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
    'gnGlobalSettings', '$location', 'gnUtilityService',
    'gnSessionService', 'gnLangs', 'gnAdminMenu',
    'gnViewerSettings', 'gnSearchSettings', '$cookies',
    function($scope, $http, $q, $rootScope, $translate,
             gnSearchManagerService, gnConfigService, gnConfig,
             gnGlobalSettings, $location, gnUtilityService,
             gnSessionService, gnLangs, gnAdminMenu,
             gnViewerSettings, gnSearchSettings, $cookies) {
      $scope.version = '0.0.1';


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
      } catch (e) {
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
      gnGlobalSettings.nodeId = $scope.nodeId;

      // Lang names to be displayed in language selector
      $scope.langLabels = {'eng': 'English', 'dut': 'Nederlands',
        'fre': 'Français', 'ger': 'Deutsch', 'kor': '한국의',
        'spa': 'Español', 'cat': 'Català', 'cze': 'Czech',
        'ita': 'Italiano', 'fin': 'Suomeksi', 'ice': 'Íslenska',
        'rus': 'русский', 'chi': '中文'};
      $scope.url = '';
      $scope.gnUrl = gnGlobalSettings.gnUrl;
      $scope.gnCfg = gnGlobalSettings.gnCfg;
      $scope.proxyUrl = gnGlobalSettings.proxyUrl;
      $scope.logoPath = gnGlobalSettings.gnUrl + '../images/harvesting/';
      $scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
      $scope.isDebug = window.location.search.indexOf('debug') !== -1;
      $scope.shibbolethEnabled = gnGlobalSettings.shibbolethEnabled;


      $scope.layout = {
        hideTopToolBar: false
      };

      /**
       * CSRF support
       */

      //Comment the following lines if you want to remove csrf support
      $http.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';
      $http.defaults.xsrfCookieName = 'XSRF-TOKEN';
      $scope.$watch(function() {
        return $cookies.get($http.defaults.xsrfCookieName);
      },
      function(value) {
        $rootScope.csrf = value;
      });
      //If no csrf, ask for one:
      if (!$rootScope.csrf) {
        $http.post('info?type=me');
      }
      //Comment the upper lines if you want to remove csrf support

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

      // login url for inline signin form in top toolbar
      $scope.signInFormAction = '../../signin#' + $location.path();

      // when the login input have focus, do not close the dropdown/popup
      $scope.focusLoginPopup = function() {
        $('.signin-dropdown #inputUsername, .signin-dropdown #inputPassword')
            .one('focus', function() {
              $(this).parents('.dropdown-menu').addClass('show');
            });
        $('.signin-dropdown #inputUsername, .signin-dropdown #inputPassword')
            .one('blur', function() {
              $(this).parents('.dropdown-menu').removeClass('show');
            });
      };

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
        // append a random number to avoid caching in IE11
        var userLogin = catInfo.then(function(value) {
          return $http.get('../api/me?_random=' +
              Math.floor(Math.random() * 10000)).
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
