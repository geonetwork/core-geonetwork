(function() {
  goog.provide('gn_wfsfilter');

  goog.require('gn_wfsfilter_directive');
  goog.require('gn_wfsfilter_service');


  var module = angular.module('gn_wfsfilter', [
    'gn_wfsfilter_directive',
    'gn_wfsfilter_service'
  ]);

})();
