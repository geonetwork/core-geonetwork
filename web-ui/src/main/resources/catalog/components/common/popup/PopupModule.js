(function() {
  goog.provide('gn_popup');


  goog.require('gn_popup_directive');
  goog.require('gn_popup_service');

  angular.module('gn_popup', [
                              'gn_popup_service',
                              'gn_popup_directive'
  ]);
})();
