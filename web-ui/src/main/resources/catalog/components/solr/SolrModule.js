(function() {
  goog.provide('gn_solr');
  goog.require('gn_solr_data_view_directive');
  goog.require('gn_solr_service');

  angular.module('gn_solr', [
    'gn_solr_data_view_directive',
    'gn_solr_service']);
})();
