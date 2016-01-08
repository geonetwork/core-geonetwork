(function() {
  goog.provide('gn_filestore');


  goog.require('gn_filestore_directive');
  goog.require('gn_filestore_service');

  angular.module('gn_filestore', [
    'gn_filestore_service', 'gn_filestore_directive'
  ]);
})();
