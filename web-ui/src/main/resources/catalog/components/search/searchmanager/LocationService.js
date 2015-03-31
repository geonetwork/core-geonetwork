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

      var state = {};

      /** ---- get methods from $location ---- **/
      this.absUrl = function() {
        return $location.absUrl();
      };
      this.host = function() {
        return $location.host();
      };
      this.path = function(path) {
        return $location.path(path);
      };
      /** ---- **/

      this.isSearch = function() {
        return $location.path() == this.SEARCH;
      };

      this.isMdView = function(path) {
        var p = path || $location.path();
        return p.indexOf(this.METADATA) == 0;
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
        this.removeParams();
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

      this.restoreSearch = function() {
        this.setSearch(state.lastSearchParams);
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

      var that = this;

      /**
       * Keep history and state of routing for to keep the search state.
       * Actually, if you had run a search, then moved to another location,
       * when you get back to the search, the params are kept and the search
       * is not fired again.
       */
      var initSearchRouting = function(evt, newUrl, oldUrl) {
        state.old = state.current || {path: ''};
        state.current = {
          params: $location.search(),
          path: $location.path()
        };
        if(that.isMdView(state.old.path) &&
            state.current.path == that.SEARCH) {
          $rootScope.$broadcast('closeMdView');
        }
        if(state.old.path == that.SEARCH &&
            state.current.path != that.SEARCH) {
          state.lastSearchParams = state.old.params;
          that.lastSearchUrl = oldUrl;
        }
      };
      initSearchRouting();
      $rootScope.$on('$locationChangeSuccess', initSearchRouting);

    }
  ]);
})();
