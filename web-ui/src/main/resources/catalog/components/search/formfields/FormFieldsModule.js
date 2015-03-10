(function() {
  goog.provide('gn_formfields');

  goog.require('gn_formfields_directive');
  goog.require('gn_formfields_service');

  angular.module('gn_formfields', [
    'gn_formfields_directive',
    'gn_formfields_service'
  ]);
})();
