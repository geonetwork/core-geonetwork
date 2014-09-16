(function() {
  goog.provide('gn_module');

  goog.require('gn');
  goog.require('gn_cat_controller');
  goog.require('gn_locale');
  goog.require('gn_form_fields_directive');
  goog.require('gn_language_switcher');
  goog.require('gn_map');
  goog.require('gn_metadata_manager');
  goog.require('gn_needhelp');
  goog.require('gn_pagination');
  goog.require('gn_search_form_controller');
  goog.require('gn_search_manager');
  goog.require('gn_utility');
  goog.require('gn_alert');

  /**
   * GnModule just manage angular injection with
   * other commonly shared modules.
   * Is used in mostly all uis, and provide common
   * components.
   *
   * @type {module|*}
   */
  var module = angular.module('gn_module', [
    'gn',
    'ngRoute',
    'gn_language_switcher',
    'gn_utility',
    'gn_search_manager',
    'gn_metadata_manager',
    'gn_pagination',
    'gn_cat_controller',
    'gn_form_fields_directive',
    'gn_map',
    'gn_search_form_controller',
    'gn_needhelp',
    'gn_alert'
  ]);

})();
