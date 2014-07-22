(function() {
  goog.provide('gn_search');

  goog.require('gn');
  goog.require('gn_resultsview');
  goog.require('gn_map_field_directive');

  var module = angular.module('gn_search', [
    'gn',
    'gn_resultsview',
    'gn_map_field_directive',
    'ui.bootstrap.buttons',
    'go'
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


      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);
})();
