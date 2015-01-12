(function() {
  goog.provide('gn_mdview');


  goog.require('gn_mdview_directive');
  goog.require('gn_mdview_service');

  angular.module('gn_mdview', [
    'gn_mdview_service',
    'gn_mdview_directive'
  ]);
})();
