(function() {
  goog.provide('gn_validation');


  goog.require('gn_validation_report_directive');
  goog.require('gn_validation_service');

  angular.module('gn_validation', [
    'gn_validation_service',
    'gn_validation_report_directive'
  ]);
})();
