(function() {
  goog.provide('gn_login');





  goog.require('gn_cat_controller');
  goog.require('gn_login_controller');
  goog.require('gn_translation');
  goog.require('gn_translation_controller');

  var module = angular.module('gn', [
    'pascalprecht.translate',
    'gn_translation_controller',
    'gn_login_controller',
    'gn_cat_controller'
  ]);

  module.config(['$translateProvider', function($translateProvider) {
    $translateProvider.useStaticFilesLoader({
      prefix: 'locales/',
      suffix: '.json'
    });

    var language = (navigator.userLanguage || navigator.language).split('-');
    // TODO : Add URL parameter to set UI language
    $translateProvider.preferredLanguage(language[0]);
  }]);
})();
