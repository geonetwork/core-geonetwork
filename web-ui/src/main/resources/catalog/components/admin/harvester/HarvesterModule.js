(function() {
  goog.provide('gn_harvester');

  goog.require('gn_harvester_directive');
  goog.require('gn_harvestervalidation_directive');

  angular.module('gn_harvester', [
    'gn_harvester_directive',
    'gn_harvestervalidation_directive'
  ]);
})();
