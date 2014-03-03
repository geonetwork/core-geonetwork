(function() {
  goog.provide('gn_map');


  goog.require('gn_map_directive');
  goog.require('gn_map_service');

  angular.module('gn_map', [
    'gn_map_service',
    'gn_map_directive'
  ]);
})();
