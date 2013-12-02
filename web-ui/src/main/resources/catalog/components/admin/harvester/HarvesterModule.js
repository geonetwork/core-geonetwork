(function() {
  geonet.provide('gn_harvester');

  geonet.require('gn_harvester_directive');

  angular.module('gn_harvester', [
    'gn_harvester_directive'
  ]);
})();
