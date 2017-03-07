(function() {
  goog.provide('gn_contact_us');


  goog.require('gn_contact_us_controller');
  goog.require('gn_module');

  var module = angular.module('gn_contact_us', [
    'gn_module',
    'gn_contact_us_controller'
  ]);

  //Define the translation files to load
  module.constant('$LOCALES', ['core']);

  module.config(['$translateProvider', '$LOCALES', '$LOCALE_MAP',
                 function($translateProvider, $LOCALES, $LOCALE_MAP) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });


      var lang = $LOCALE_MAP(location.href.split('/')[5]);
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);
})();
