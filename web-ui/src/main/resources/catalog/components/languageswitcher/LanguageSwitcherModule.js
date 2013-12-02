(function() {
  geonet.provide('gn_language_switcher');

  geonet.require('gn_language_switcher_directive');

  angular.module('gn_language_switcher', [
    'gn_language_switcher_directive'
  ]);
})();
