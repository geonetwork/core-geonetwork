(function() {
  goog.provide('gn_admin');









  goog.require('gn');
  goog.require('gn_admin_controller');
  goog.require('gn_admintools_controller');
  goog.require('gn_cat_controller');
  goog.require('gn_dashboard_controller');
  goog.require('gn_translation');
  goog.require('gn_translation_controller');
  goog.require('gn_classificationSystems_controller');

  var module = angular.module('gn_admin', [
    'gn',
    'gn_admin_controller'
  ]);

})();
