(function() {
  geonet.provide('gn_pagination');

  geonet.require('gn_pagination_directive');

  angular.module('gn_pagination', [
    'gn_pagination_directive'
  ]);
})();
