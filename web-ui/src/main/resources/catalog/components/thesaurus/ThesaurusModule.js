(function() {
  goog.provide('gn_thesaurus');



  goog.require('gn_keyword_selector');
  goog.require('gn_thesaurus_selector');
  goog.require('gn_thesaurus_service');

  angular.module('gn_thesaurus', [
    'gn_thesaurus_service',
    'gn_thesaurus_selector',
    'gn_keyword_selector'
  ]);
})();
