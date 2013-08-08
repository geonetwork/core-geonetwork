(function() {
  goog.provide('gn');









  goog.require('gn_cat_controller');
  goog.require('gn_metadata_manager');
  goog.require('gn_pagination');
  goog.require('gn_search_manager');
  goog.require('gn_search_results');
  goog.require('gn_translation');
  goog.require('gn_translation_controller');

  var module = angular.module('gn', [
    'pascalprecht.translate',
    'gn_search_manager',
    'gn_metadata_manager',
    'gn_search_results',
    'gn_pagination',
    'gn_translation_controller',
    'gn_cat_controller'
  ]);

  module.config(['$translateProvider', function($translateProvider) {
    $translateProvider.useStaticFilesLoader({
      prefix: '../../catalog/locales/',
      suffix: '.json'
    });

    var language = (navigator.userLanguage || navigator.language).split('-');
    // TODO : Add URL parameter to set UI language
    $translateProvider.preferredLanguage(language[0]);
  }]);

})();
