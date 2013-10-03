(function() {
  goog.provide('gn_admin');









  goog.require('gn');
  goog.require('gn_admin_controller');

  var module = angular.module('gn_admin', [
    'gn',
    'gn_admin_controller'
  ]);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'admin']);

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
