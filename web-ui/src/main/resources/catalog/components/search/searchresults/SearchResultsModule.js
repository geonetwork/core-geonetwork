(function() {
  goog.provide('gn_search_results');

  goog.require('gn_cat_controller');
  goog.require('gn_search_results_directive');

  angular.module('gn_search_results', [
    'gn_search_results_directive'
  ]);
})();
