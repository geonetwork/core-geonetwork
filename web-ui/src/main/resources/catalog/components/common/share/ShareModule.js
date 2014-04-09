(function() {
  goog.provide('gn_share');

  goog.require('gn_share_directive');
  goog.require('gn_share_service');

  angular.module('gn_share', [
    'gn_share_directive',
    'gn_share_service'
  ]);
})();
