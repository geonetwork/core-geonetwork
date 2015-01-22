(function() {

  goog.provide('gn_mdactions');

  goog.require('gn_mdactions_service');
  goog.require('gn_mdactions_directive');

  var module = angular.module('gn_mdactions', [
    'gn_mdactions_service',
    'gn_mdactions_directive'
  ]);
})();
