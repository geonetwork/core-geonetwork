(function() {
  goog.provide('gn_cat_controller');

  var module = angular.module('gn_cat_controller', []);

  /**
   * The catalogue controller.
   *
   * A body-level scope makes sense for example:
   *
   *     <body ng-controller="GnCatController">
   */
  module.controller('GnCatController', [
    '$scope', '$http', '$q', '$rootScope', '$translate',
    function($scope, $http, $q, $rootScope, $translate) {
      $scope.version = '0.0.1';
      // TODO : add language
      $scope.lang = 'eng';
      $scope.url = '';
      $scope.base = '../../catalog/';
      $scope.pages = {
        home: 'catalog.search',
        admin: 'admin.console',
        signin: 'catalog.signin'
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

      $scope.status = {};
      var defaultStatus = {
        title: '',
        link: '',
        msg: '',
        error: '',
        type: 'info',
        timeout: -1
      };

      var promiseStart = $q.when('start');

      // Retrieve site information
      // TODO: Add INSPIRE, harvester, ... information
      var catInfo = promiseStart.then(function(value) {
        url = $scope.url + 'xml.info@json?type=site&type=auth';
        return $http.get(url).
            success(function(data, status) {
              $scope.info = data;
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
        url = $scope.url + 'xml.info@json?type=me';
        return $http.get(url).
            success(function(data, status) {
              $scope.user = data.me;
              $scope.authenticated = data.me['@authenticated'] !== 'false';
              // TODO : should not be here, redirect to home
              if ($scope.authenticated) {
              // User is logged in
              } else {
              }
            }).
            error(function(data, status, headers, config) {
                  // TODO : translate
                  $rootScope.$broadcast('StatusUpdated',
                      {msg: $translate('msgNoUserInfo')}
                  );
                });
      });

      $scope.clearStatusMessage = function() {
        $scope.status = {};
        $('.gn-info').hide();
      };

      $scope.metadataEdit = function(uuid) {
        console.log('Edit: ' + uuid);
      };
      $scope.$on('StatusUpdated', function(event, status) {
        $.extend($scope.status, defaultStatus, status);

        $('.gn-info').show();

        // TODO : handle multiple messages
        if ($scope.status.timeout > 0) {
          setTimeout(function() {
            $scope.clearStatusMessage();
          }, $scope.status.timeout * 1000);
        }
      });







      $scope.selectMetadata = function(uuid) {
        console.log('select ' + uuid);
      };
      $scope.selectAllMetadata = function() {
        console.log('select all ');
      };





    }]);

})();
