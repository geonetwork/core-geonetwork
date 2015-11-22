(function() {
  goog.provide('gn_thesaurus');





  goog.require('gn_multiselect');
  goog.require('gn_thesaurus_directive');
  goog.require('gn_thesaurus_service');
  goog.require('ngSkos');

  angular.module('gn_thesaurus', [
    'gn_multiselect',
    'ngSkos',
    'gn_thesaurus_service',
    'gn_thesaurus_directive']);
})();
