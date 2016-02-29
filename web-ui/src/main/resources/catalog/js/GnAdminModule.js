(function() {
  goog.provide('gn_admin');










  goog.require('gn_admin_controller');
  goog.require('gn_module');

  var module = angular.module('gn_admin', [
    'gn_module',
    'gn_admin_controller'
  ]);

  module.config(['$LOCALES',
                 function($LOCALES) {
                   $LOCALES.push('admin');
    }]);
})();
