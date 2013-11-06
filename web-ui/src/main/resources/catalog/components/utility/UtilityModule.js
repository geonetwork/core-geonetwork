(function() {
  goog.provide('gn_utility');

  goog.require('gn_utility_service');
  goog.require('gn_utility_directive');

  angular.module('gn_utility', [
    'gn_utility_service',
    'gn_utility_directive'
  ]);
})();
