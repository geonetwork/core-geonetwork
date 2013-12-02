(function() {
  geonet.provide('gn_category');

  geonet.require('gn_category_directive');

  angular.module('gn_category', [
    'gn_category_directive'
  ]);
})();
