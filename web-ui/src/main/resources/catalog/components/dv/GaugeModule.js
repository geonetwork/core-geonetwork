(function() {
  geonet.provide('gn_gauge');

  geonet.require('gn_gauge_directive');

  angular.module('gn_gauge', [
    'gn_gauge_directive'
  ]);
})();
