(function() {
  goog.provide('gn_openwis_subscription_module');

  goog.require('gn_openwis_subscribe_directive');
  goog.require('gn_openwis_download');
  goog.require('gn_openwis_deliver');
  goog.require('gn_openwis_subscribe');
  goog.require('gn_openwis_check_subscription_button');

  var module = angular.module('gn_openwis_subscription_module', [
      'gn_openwis_subscribe_directive', 'gn_openwis_download',
      'gn_openwis_deliver', 'gn_openwis_subscribe',
      'gn_openwis_check_subscription_button'
  ]);

})();
