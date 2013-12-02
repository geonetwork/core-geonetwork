(function() {
  geonet.provide('gn_search_manager');

  geonet.require('gn_search_manager_service');

  angular.module('gn_search_manager', [
    'gn_search_manager_service'
  ]);
})();
