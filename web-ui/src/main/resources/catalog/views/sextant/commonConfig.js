(function() {

  goog.provide('gn_search_sextant_commonconfig');

  var module = angular.module('gn');

  module.run(['gnShareConstants', function(gnShareConstants) {

    gnShareConstants.columnOrder= ['0', '5', '1', '2', '7'];
    gnShareConstants.disableAllCol = true;
    gnShareConstants.displayProfile = true;

  }])
})();
