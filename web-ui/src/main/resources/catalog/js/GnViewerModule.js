(function() {
  goog.provide('gn_viewer_module');











  goog.require('gn_batch_service');
  goog.require('gn_draggable_directive');
  goog.require('gn_editor_controller');
  goog.require('gn_geopublisher');
  goog.require('gn_module');
  goog.require('gn_onlinesrc');
  goog.require('gn_ows');
  goog.require('gn_popup');
  goog.require('gn_suggestion');
  goog.require('gn_validation');

  var module = angular.module('gn_viewer', [
    'gn_module',
    'gn_popup',
    'gn_onlinesrc',
    'gn_suggestion',
    'gn_validation',
    'gn_draggable_directive',
    'gn_editor_controller',
    'gn_ows',
    'gn_geopublisher',
    'gn_batch_service'
  ]);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'editor']);

  module.config(['$translateProvider', '$LOCALES', '$LOCALE_MAP',
                 function($translateProvider, $LOCALES, $LOCALE_MAP) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = $LOCALE_MAP(location.href.split('/')[5]);
      $translateProvider.preferredLanguage(lang);
      moment.locale(lang);
    }]);
})();
