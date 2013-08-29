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

  //Define the translation files to load
  module.constant('$LOCALES', ['core']);

  module.config(['$translateProvider', '$LOCALES',
                 function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var language = (navigator.userLanguage || navigator.language).split('-');
      // TODO : Add URL parameter to set UI language
      $translateProvider.preferredLanguage(language[0]);
    }]);
})();
