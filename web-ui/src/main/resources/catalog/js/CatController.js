(function() {
  goog.provide('gn_cat_controller');

  goog.require('gn_search_manager');

  var module = angular.module('gn_cat_controller',
      ['gn_search_manager']);


  module.constant('gnGlobalSettings', {
    proxyUrl: '../../proxy?url=',
    locale: {},
    isMapViewerEnabled: false
  });

  /**
   * The catalogue controller takes care of
   * loading site information, check user login state
   * and a facet search to get main site information.
   *
   * A body-level scope makes sense for example:
   *
   *     <body ng-controller="GnCatController">
   */
  module.controller('GnCatController', [
    '$scope', '$http', '$q', '$rootScope', '$translate',
    'gnSearchManagerService', 'gnConfigService', 'gnConfig',
    'gnGlobalSettings', '$location',
    function($scope, $http, $q, $rootScope, $translate,
            gnSearchManagerService, gnConfigService, gnConfig,
            gnGlobalSettings, $location) {
      $scope.version = '0.0.1';
      // TODO : add language
      var tokens = location.href.split('/');
      $scope.lang = tokens[5];
      $scope.nodeId = tokens[4];
      // TODO : get list from server side
      $scope.langs = {'fre': 'fr', 'eng': 'en', 'spa': 'sp'};
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
      $scope.authenticated = false;
      $scope.initialized = false;

      /**
       * Keep a reference on main cat scope
       * @return {*}
       */
      $scope.getCatScope = function() {return $scope};

      gnConfigService.load().then(function(c) {
        // Config loaded
        //gnMap.importProj4js();
        // TODO: make map proj load in mapService.config instead of here
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
          url = $scope.url + 'info?_content_type=json&type=site&type=auth';
          return $http.get(url).
              success(function(data, status) {
                $scope.info = data;
                // Add the last time catalog info where updated.
                // That could be useful to append to catalog image URL
                // in order to trigger a reload of the logo when info are
                // reloaded.
                $scope.info.site.lastUpdate = new Date();
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
            return this['@authenticated'] === 'false';
          },
          isConnected: function() {
            return !this.isAnonymous();
          },
          canEditRecord: function(md) {
            if (md === null) {
              return false;
            }

            // The md provide the information about
            // if the current user can edit records or not.
            var editable = md['geonet:info'].edit == 'true';


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
          url = $scope.url + 'info?_content_type=json&type=me';
          return $http.get(url).
              success(function(data, status) {
                $scope.user = data.me;
                angular.extend($scope.user, userFn);

                $scope.authenticated = data.me['@authenticated'] !== 'false';
              }).
              error(function(data, status, headers, config) {
                // TODO : translate
                $rootScope.$broadcast('StatusUpdated',
                   {msg: $translate('msgNoUserInfo')}
                );
              });
        });


        // Retrieve main search information
        var searchInfo = userLogin.then(function(value) {
          url = 'qi?_content_type=json&summaryOnly=true';
          return gnSearchManagerService.search(url).
              then(function(data) {
                $scope.searchInfo = data;
              });
        });
      };


      $scope.$on('loadCatalogInfo', function(event, status) {
        $scope.loadCatalogInfo();
      });

      $scope.clearStatusMessage = function() {
        $scope.status = null;
        $('.gn-info').hide();
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



      $scope.loadCatalogInfo();


    }]);

})();
