(function() {
  goog.provide('gn_search_location');

  var module = angular.module('gn_search_location', []);

  module.service('gnSearchLocation', [
    '$location',
    '$rootScope',
    function($location, $rootScope) {

      this.SEARCH = '/search';
      this.MAP = '/map';
      this.METADATA = '/metadata/';

      this.absUrl = $location.absUrl;

      this.isSearch = function() {
        return $location.path() == this.SEARCH;
      };

      this.isMdView = function() {
        return $location.path().indexOf(this.METADATA) == 0;
      };

      this.isMap = function() {
        return $location.path() == this.MAP;
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
        $location.path(this.MAP);
      };

      this.setSearch = function(params) {
        $location.path(this.SEARCH);
        $location.search(params);
      };
      this.removeParams = function() {
        $location.search('');
      };
      this.getParams = function() {
        return $location.search();
      };

      this.initTabRouting = function(tabs) {
        var that = this;
        var updateTabs = function() {
          if (that.isSearch()) {
            tabs.search.active = true;
          }
          else if (that.isMap()) {
            tabs.map.active = true;
          }
          else if (that.isMdView()) {
            tabs.view.active = true;
          }
        };
        updateTabs();
        $rootScope.$on('$locationChangeSuccess', updateTabs);
      };
    }
  ]);


})();
