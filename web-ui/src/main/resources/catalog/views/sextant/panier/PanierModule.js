(function() {
  goog.provide('sxt_panier');

  goog.require('sxt_panier_directive');
  goog.require('sxt_panier_service');

  var module = angular.module('sxt_panier', [
    'sxt_panier_directive',
    'sxt_panier_service'
  ]);
})();
