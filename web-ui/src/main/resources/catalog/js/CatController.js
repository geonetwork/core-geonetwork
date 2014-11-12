(function() {
  goog.provide('gn_cat_controller');

  goog.require('gn_search_manager');

  var module = angular.module('gn_cat_controller', ['gn_search_manager']);

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
    'gnSearchManagerService', 'gnConfigService',
    'gnMap',
    function($scope, $http, $q, $rootScope, $translate,
            gnSearchManagerService, gnConfigService, gnMap) {
      $scope.version = '0.0.1';
      // TODO : add language
      var tokens = location.href.split('/');
      $scope.lang = tokens[5];
      $scope.nodeId = tokens[4];
      // TODO : get list from server side
      $scope.langs = {'fre': 'fr', 'eng': 'en', 'spa': 'sp'};
      $scope.url = '';
      $scope.base = '../../catalog/';
      $scope.proxyUrl = '../../proxy?url=';
      $scope.logoPath = '../../images/harvesting/';
      $scope.pages = {
        home: 'home',
        admin: 'admin.console',
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

      gnConfigService.load().then(function(c) {
        // Config loaded
        gnMap.importProj4js();
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
          url = $scope.url + 'info@json?type=site&type=auth';
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



        // Retrieve user information if catalog is online
        var userLogin = catInfo.then(function(value) {
          url = $scope.url + 'info@json?type=me';
          return $http.get(url).
              success(function(data, status) {
                $scope.user = data.me;
                $scope.authenticated = data.me['@authenticated'] !== 'false';
                // TODO : should not be here, redirect to home
                //                  if ($scope.authenticated) {
                //                  // User is logged in
                //                  } else {
                //                  }
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
          url = 'qi@json?summaryOnly=true';
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
