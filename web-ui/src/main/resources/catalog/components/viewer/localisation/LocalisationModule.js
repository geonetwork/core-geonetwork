(function() {
  goog.provide('gn_localisation');

  goog.require('gn_localisation_directive');
  goog.require('gn_localisation_service');

  var module = angular.module('gn_localisation', [
    'gn_localisation_directive',
    'gn_localisation_service'
  ]);

})();
