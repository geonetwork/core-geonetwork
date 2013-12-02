(function() {
  geonet.provide('gn_search_results');

  geonet.require('gn_cat_controller');
  geonet.require('gn_search_results_directive');

  angular.module('gn_search_results', [
    'gn_search_results_directive'
  ]);
})();
