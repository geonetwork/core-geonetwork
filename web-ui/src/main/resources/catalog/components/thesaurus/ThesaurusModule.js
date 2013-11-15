(function() {
  goog.provide('gn_thesaurus');



  goog.require('gn_thesaurus_service');
  goog.require('gn_thesaurus_directive');

  angular.module('gn_thesaurus', [
    'gn_thesaurus_service',
    'gn_thesaurus_directive'
  ]);
})();
