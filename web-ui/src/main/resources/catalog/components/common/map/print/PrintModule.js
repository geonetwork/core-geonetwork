(function() {
  goog.provide('gn_print');

  goog.require('gn_printmap_directive');
  goog.require('gn_printmap_service');


  var module = angular.module('gn_print', [
    'gn_printmap_directive',
    'gn_printmap_service'
  ]);

})();
