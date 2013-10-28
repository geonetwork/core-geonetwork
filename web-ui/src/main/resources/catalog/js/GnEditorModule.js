(function() {
  goog.provide('gn_editor');


  goog.require('gn');
  goog.require('gn_editor_controller');

  var module = angular.module('gn_editor', [
    'gn',
    'gn_editor_controller'
  ]);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'editor']);

  module.config(['$translateProvider', '$LOCALES',
                 function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);
})();
