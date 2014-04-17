(function() {
  goog.provide('gn_editor');






























  goog.require('gn');
  goog.require('gn_batch_service');
  goog.require('gn_draggable_directive');
  goog.require('gn_editor_controller');
  goog.require('gn_editor_directive');
  goog.require('gn_geopublisher');
  goog.require('gn_onlinesrc');
  goog.require('gn_ows');
  goog.require('gn_popup');
  goog.require('gn_suggestion');
  goog.require('gn_validation');

  var module = angular.module('gn_editor', [
    'gn',
    'gn_popup',
    'gn_onlinesrc',
    'gn_suggestion',
    'gn_validation',
    'gn_draggable_directive',
    'gn_editor_controller',
    'gn_ows',
    'gn_geopublisher',
    'gn_batch_service',
    'gn_editor_directive'
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
