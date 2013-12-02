(function() {
  geonet.provide('gn_metadata_manager');

  geonet.require('gn_metadata_manager_service');

  angular.module('gn_metadata_manager', [
    'gn_metadata_manager_service'
  ]);
})();
