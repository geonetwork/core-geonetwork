(function() {
  goog.provide('gn_contact_us');


  goog.require('gn_contact_us_controller');
  goog.require('gn_module');

  var module = angular.module('gn_contact_us', [
    'gn_module',
    'gn_contact_us_controller'
  ]);

})();
