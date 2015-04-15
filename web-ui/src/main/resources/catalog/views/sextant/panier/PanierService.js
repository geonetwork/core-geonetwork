(function() {
  goog.provide('sxt_panier_service');

  var module = angular.module('sxt_panier_service', [
  ]);


  module.service('sxtPanierService', [
      '$http',
    function($http) {

      var panierUrl = 'extractor.doExtract';

      var callExtractService = function(panier) {
        $http({
          url: panierUrl,
          method: 'POST',
          data: panierUrl,
          headers: {'Content-Type': 'application/xml'}
        }).then(function(data) {

        });
      };

      this.extract = function(panier) {

/*
        var output = [];
        angular.forEach(panier, function(elt) {
          output.push({
            id: elt.md.getUuid()
          })
        });
        console.log(output);
*/
        callExtractService(panier);
      };
    }
  ]);

})();
