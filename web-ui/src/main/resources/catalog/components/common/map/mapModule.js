(function() {
  goog.provide('gn_map');


  goog.require('gn_map_directive');
  goog.require('gn_map_service');
  goog.require('gn_map_wmsqueue');

  angular.module('gn_map', [
    'gn_map_service',
    'gn_map_directive',
    'gn_map_wmsqueue'
  ]);
})();
