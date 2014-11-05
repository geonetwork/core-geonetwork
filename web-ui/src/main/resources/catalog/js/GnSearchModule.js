(function() {
  goog.provide('gn_search');

  goog.require('gn_module');
  goog.require('gn_resultsview');
  goog.require('gn_map_field_directive');
  goog.require('gn_viewer');
  goog.require('gn_search_controller');

  var module = angular.module('gn_search', [
    'gn_module',
    'gn_resultsview',
    'gn_map_field_directive',
    'gn_search_controller',
    'gn_viewer',
    'ui.bootstrap.buttons',
    'ui.bootstrap.tabs',
    'go'
  ]);

  module.constant('gnSearchSettings', {});
  module.constant('gnViewerSettings', {
     proxyUrl: '../../proxy?url='
  });

  //Define the translation files to load
  module.constant('$LOCALES', ['core', 'search']);


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
