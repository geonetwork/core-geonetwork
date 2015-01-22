(function() {
  goog.provide('gn_search_manager');


  goog.require('gn_search_location');
  goog.require('gn_search_manager_service');

  angular.module('gn_search_manager', [
    'gn_search_manager_service',
    'gn_search_location'
  ]);
})();
