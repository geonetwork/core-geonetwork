(function() {
  geonet.provide('gn_dbtranslation');

  geonet.require('gn_dbtranslation_directive');

  angular.module('gn_dbtranslation', [
    'gn_dbtranslation_directive'
  ]);
})();
