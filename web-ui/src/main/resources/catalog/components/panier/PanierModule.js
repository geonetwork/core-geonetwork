(function() {
  goog.provide('sxt_panier');

  goog.require('gn_module');
  goog.require('sxt_panier_directive');

  var module = angular.module('sxt_panier', [
    'gn_module',
    'sxt_panier_directive'
  ]);
})();
