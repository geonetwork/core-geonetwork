(function() {
  goog.provide('gn_ows');


  goog.require('gn_ows_directive');
  goog.require('gn_ows_service');

  angular.module('gn_ows', [
    'gn_ows_service',
    'gn_ows_directive'
  ]);
})();
