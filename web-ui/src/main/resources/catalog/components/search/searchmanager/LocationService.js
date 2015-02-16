(function() {
  goog.provide('gn_search_location');

  var module = angular.module('gn_search_location', []);

  module.service('gnSearchLocation', [
    '$location',
    '$rootScope',
    'gnGlobalSettings',
    function($location, $rootScope, gnGlobalSettings) {

      this.SEARCH = '/search';
      this.MAP = '/map';
      this.METADATA = '/metadata/';
      this.HOME = '/home';

      this.absUrl = function() {
        return $location.absUrl();
      };

      this.isSearch = function() {
        return $location.path() == this.SEARCH;
      };

      this.isMdView = function() {
        return $location.path().indexOf(this.METADATA) == 0;
      };

      this.isMap = function() {
        return $location.path() == this.MAP;
      };

      this.isHome = function() {
        return $location.path() == this.HOME;
      };

      this.isUndefined = function() {
        return angular.isUndefined($location.path()) ||
            $location.path() == '';
      };

      this.saveLastUrl = function() {
        this.lastSearchUrl = $location.absUrl();
      };

      this.setUuid = function(uuid) {
        $location.path(this.METADATA + uuid);
      };

      this.getUuid = function() {
        if (this.isMdView()) {
          var url = $location.path();
          return url.substring(this.METADATA.length, url.length);
        }
      };

      this.setMap = function() {
        if (gnGlobalSettings.isMapViewerEnabled) {
          $location.path(this.MAP);
        }
      };

      this.setSearch = function(params) {
        $location.path(this.SEARCH);
        if (params) {
          $location.search(params);
        }
      };
      this.removeParams = function() {
        $location.search('');
      };
      this.getParams = function() {
        return $location.search();
      };

      this.setHome = function() {
        $location.path(this.HOME);
        $location.search({});
      };

      this.initTabRouting = function(tabs) {
        var that = this;
        var updateTabs = function() {
          var tab = $location.path().
              match(/^\/([a-zA-Z0-9]*)($|\/.*)/)[1];

          tabs[tab].active = true;
        };
        updateTabs();
        $rootScope.$on('$locationChangeSuccess', updateTabs);
      };
    }
  ]);
})();
