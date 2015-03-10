(function() {

  goog.provide('gn_mdactions');


  goog.require('gn_mdactions_directive');
  goog.require('gn_mdactions_service');

  var module = angular.module('gn_mdactions', [
    'gn_mdactions_service',
    'gn_mdactions_directive'
  ]);
})();
