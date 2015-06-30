(function() {
  goog.provide('gn_login');




  goog.require('gn');
  goog.require('gn_cat_controller');
  goog.require('gn_login_controller');

  var module = angular.module('gn_login', [
    'gn',
    'gn_login_controller',
    'gn_cat_controller'
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
