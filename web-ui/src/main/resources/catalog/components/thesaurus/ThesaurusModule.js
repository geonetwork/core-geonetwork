(function() {
  goog.provide('gn_thesaurus');

  goog.require('gn_multiselect');
	goog.require('ngSkos');
  goog.require('gn_thesaurus_directive');
  goog.require('gn_thesaurus_service');

  angular.module('gn_thesaurus', [
    'gn_multiselect',
		'ngSkos',
    'gn_thesaurus_service',
    'gn_thesaurus_directive']);
})();
