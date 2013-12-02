(function() {
  geonet.provide('gn_thesaurus_type');

  geonet.require('gn_thesaurus_type_directive');

  angular.module('gn_thesaurus_type', [
    'gn_thesaurus_type_directive'
  ]);
})();
