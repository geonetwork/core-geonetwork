(function() {
  goog.provide('cookie_warning');

  goog.require('cookie_warning_controller');
  goog.require('cookie_warning_directive');

  var module = angular.module('cookie_warning', ['cookie_warning_controller',
    'cookie_warning_directive']);

})();
