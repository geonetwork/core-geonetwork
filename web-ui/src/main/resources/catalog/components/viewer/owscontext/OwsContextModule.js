(function () {
  goog.provide('gn_owscontext');

  goog.require('gn_owscontext_service');
  goog.require('gn_owscontext_directive');

  var module = angular.module('gn_owscontext', [
      'gn_owscontext_service',
      'gn_owscontext_directive'
  ]);
})();
