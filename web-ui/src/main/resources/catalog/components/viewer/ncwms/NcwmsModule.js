(function () {
  goog.provide('gn_ncwms');

  goog.require('gn_ncwms_service');
  goog.require('gn_ncwms_directive');

  var module = angular.module('gn_ncwms', [
      'gn_ncwms_service',
      'gn_ncwms_directive',
      'ui.slider'
  ]);
})();
