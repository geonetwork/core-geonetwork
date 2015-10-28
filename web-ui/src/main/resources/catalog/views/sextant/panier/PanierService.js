(function() {
  goog.provide('sxt_panier_service');

  var module = angular.module('sxt_panier_service', [
  ]);


  module.service('sxtPanierService', [
      '$http',
    function($http) {

      var panierUrl = 'extractor.doExtract';

      var callExtractService = function(panier) {
        return $http({
          url: panierUrl,
          method: 'POST',
          data: panier,
          headers: {'Content-Type': 'application/json'}
        });
      };

      this.extract = function(panier) {
        return callExtractService(panier);
      };
    }
  ]);

})();
