(function() {
  goog.provide('gn_onlinesrc');


  goog.require('gn_onlinesrc_directive');
  goog.require('gn_onlinesrc_service');

  angular.module('gn_onlinesrc', [
    'gn_onlinesrc_service',
    'gn_onlinesrc_directive'
  ]);
})();
