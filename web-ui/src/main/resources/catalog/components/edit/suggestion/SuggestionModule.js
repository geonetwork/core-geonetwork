(function() {
  goog.provide('gn_suggestion');

  goog.require('gn_suggestion_directive');
  goog.require('gn_suggestion_service');

  angular.module('gn_suggestion', [
    'gn_suggestion_directive',
    'gn_suggestion_service'
  ]);
})();
