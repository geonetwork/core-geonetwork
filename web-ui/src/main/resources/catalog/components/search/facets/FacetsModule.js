(function() {
  goog.provide('gn_facets');





  goog.require('gn_facets_config_service');
  goog.require('gn_facets_directive');
  goog.require('gn_facets_service');

  angular.module('gn_facets', [
    'gn_facets_directive',
    'gn_facets_config_service',
    'gn_facets_service'
  ]);
})();
