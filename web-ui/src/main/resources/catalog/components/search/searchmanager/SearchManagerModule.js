(function() {
  goog.provide('gn_search_manager');

  goog.require('gn_search_manager_service');
  goog.require('gn_search_location');

  angular.module('gn_search_manager', [
    'gn_search_manager_service',
    'gn_search_location'
  ]);
})();
