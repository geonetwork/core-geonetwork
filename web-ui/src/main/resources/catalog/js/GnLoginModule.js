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

})();
