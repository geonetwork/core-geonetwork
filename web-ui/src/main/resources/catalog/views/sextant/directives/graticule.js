(function() {
  goog.provide('sxt_graticule');

  var module = angular.module('sxt_graticule', []);


  var SxtGraticuleController = function($scope) {

    var layer = new ol.layer.Image({
      source: new ol.source.ImageWMS({
        url: 'http://sextant-test.ifremer.fr/cgi-bin/sextant/wms/graticule',
        params: {'LAYERS': 'graticule_4326'},
        ratio: 1
      })
    });
    layer.background = true; // do not save it in context

    //layer.setZIndex(10);

    $scope.$watch(function() {
      return this.active;
    }.bind(this), function() {
      if(this.active === true) {
        this.map.addLayer(layer);
      }
      else {
        this.map.removeLayer(layer);
      }
    }.bind(this));
  };
  SxtGraticuleController.$inject = ['$scope'];

  module.directive('sxtGraticule', [
    'sxtGlobals',
    'gnHttp',
    function(sxtGlobals, gnHttp) {
      return {
        bindToController: {
          map: '<sxtMap',
          active: '<sxtActive'
        },
        controller: SxtGraticuleController,
        controllerAs: 'ctrl'
      };
    }]);

})();
