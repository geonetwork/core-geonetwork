(function() {
  goog.provide('gn_openwis_subscription_module');

  goog.require('gn_openwis_request_cache_directive');
  goog.require('gn_openwis_request_directive');
  goog.require('gn_openwis_subscribe_directive');
  goog.require('gn_openwis_request_cache');

  var module = angular.module('gn_openwis_subscription_module', 
      ['gn_openwis_request_cache_directive',
       'gn_openwis_request_directive',
       'gn_openwis_subscribe_directive',
       'gn_openwis_request_cache']);

})();
