(function() {
  goog.provide('gn_geopublisher');


  goog.require('gn_geopublisher_directive');
  goog.require('gn_geopublisher_service');
  goog.require('gn_map');

  angular.module('gn_geopublisher', [
    'gn_geopublisher_directive',
    'gn_geopublisher_service',
    'gn_map'
  ]);
})();
