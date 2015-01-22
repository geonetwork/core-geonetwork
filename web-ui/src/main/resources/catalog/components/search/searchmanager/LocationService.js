(function() {
  goog.provide('gn_search_location');

  var module = angular.module('gn_search_location', []);

  module.service('gnSearchLocation', [
    '$location',
    function($location) {

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

      this.setUuid = function(uuid) {
        $location.path(this.METADATA + uuid);
      };

      this.getUuid = function() {
        if(this.isMdView()) {
          var url = $location.path();
          return url.substring(this.METADATA.length, url.length);
        }
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
      }
    }
  ]);


})();
