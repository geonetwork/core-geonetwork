(function() {
  goog.provide('gn_login');



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


      var pathArray = window.location.pathname.split('/');
      var lang = pathArray[3][0] + pathArray[3][1];

      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);
})();
