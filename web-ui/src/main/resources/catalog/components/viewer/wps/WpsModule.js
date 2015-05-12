(function() {
  goog.provide('gn_wps');

  goog.require('gn_wps_directive');
  goog.require('gn_wps_service');

  var module = angular.module('gn_wps', [
    'gn_wps_service',
    'gn_wps_directive'
  ]);
})();
