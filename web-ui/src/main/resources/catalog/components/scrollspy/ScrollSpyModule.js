(function() {
  geonet.provide('gn_scroll_spy');

  geonet.require('gn_scroll_spy_directive');

  angular.module('gn_scroll_spy', [
    'gn_scroll_spy_directive'
  ]);
})();
