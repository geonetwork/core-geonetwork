(function() {
  goog.provide('gn');

  goog.require('gn_locale');

  /**
   * Main gn module.
   *
   * Must be included by all uis.
   * Is used in wro4j for $templatecache.
   * Must contains only what is mendatory in all uis:
   * - locale management
   *
   * @type {module|*}
   */
  var module = angular.module('gn', [
    'gn_locale', 'cfp.hotkeys'
  ]);

})();
