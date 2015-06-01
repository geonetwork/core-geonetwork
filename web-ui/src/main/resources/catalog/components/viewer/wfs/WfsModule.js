(function() {
  goog.provide('gn_wfs');

  goog.require('gn_wfs_directive');
  goog.require('gn_wfs_service');

  var module = angular.module('gn_wfs', [
    'gn_wfs_service',
    'gn_wfs_directive'
  ]);
})();
