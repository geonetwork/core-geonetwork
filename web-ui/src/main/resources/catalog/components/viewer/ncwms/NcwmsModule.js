(function() {
  goog.provide('gn_ncwms');


  goog.require('gn_ncwms_directive');
  goog.require('gn_ncwms_service');

  var module = angular.module('gn_ncwms', [
    'gn_ncwms_service',
    'gn_ncwms_directive',
    'ui.slider'
  ]);
})();
