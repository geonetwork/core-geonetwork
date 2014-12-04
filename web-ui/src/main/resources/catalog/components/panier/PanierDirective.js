(function() {
  goog.provide('sxt_panier_directive');

  var module = angular.module('sxt_panier_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name sxt_panier_directive.directive:sxtPanier
   *
   * @description
   */
  module.directive('sxtPanier', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          panier: '=sxtPanier'
        },
        templateUrl: '../../catalog/components/panier/' +
            'partials/panier.html',
        controller: ['$scope', function($scope) {
          this.del = function(md) {
            $scope.panier.splice($scope.panier.indexOf(md), 1);
          };
        }],
        link: function(scope, element, attrs, controller) {

          scope.formObj = [];
          scope.extract = function() {
            console.log(scope.formObj);
          };
        }
      };
    }]);

  module.directive('sxtPanierElt', [
    'gnMap',
    'gnSearchSettings',
    'gnPanierSettings',
    function(gnMap, gnSearchSettings, gnPanierSettings) {
      return {
        restrict: 'A',
        require: '^sxtPanier',
        replace: true,
        scope: {
          formObj: '=sxtPanierElt',
          md: '=sxtPanierEltMd'
        },
        templateUrl: '../../catalog/components/panier/' +
            'partials/panierelement.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {

              /** To iniate combo box values */
              scope.settings = gnPanierSettings;

              /** Use to know if we need to zoom on the md extent or not */
              var rendered = false;

              /** object that contains the form values */
              scope.form = {
                uuid: scope.md.getUuid(),
                format: gnPanierSettings.defaults.format,
                projection: gnPanierSettings.defaults.proj
              };

              /** Map useed to draw the bbox */
              scope.map = new ol.Map({
                layers: [
                  new ol.layer.Tile({
                    source: new ol.source.OSM()
                  })
                ],
                view: new ol.View({
                  center: [0, 0],
                  zoom: 2
                })
              });

              // Set initial extent to draw the BBOX
              var extent = gnMap.getBboxFromMd(scope.md);
              if (extent) {

                // Fixed feature overlay to show extent of the md
                var feature = new ol.Feature();
                var featureOverlay = new ol.FeatureOverlay({
                  style: gnSearchSettings.olStyles.mdExtent
                });
                featureOverlay.setMap(scope.map);
                featureOverlay.addFeature(feature);

                var proj = scope.map.getView().getProjection();
                extent = ol.extent.containsExtent(proj.getWorldExtent(),
                    extent) ?
                    ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                    proj.getExtent();

                // Set coords in the scope to pass it to the mapField directive
                scope.extentCoords = gnMap.getPolygonFromExtent(extent);
                feature.setGeometry(new ol.geom.Polygon(scope.extentCoords));
              }

              // To update size on first maps render
              scope.$on('renderPanierMap', function() {
                scope.map.updateSize();
                if (feature && !rendered) {
                  scope.map.getView().fitExtent(
                      feature.getGeometry().getExtent(), scope.map.getSize());
                }
                rendered = true;
              });
            },
            post: function preLink(scope, iElement, iAttrs, controller) {

              scope.formObj.push(scope.form);

              scope.del = function(md, form) {
                controller.del(md);
                scope.formObj.splice(scope.formObj.indexOf(form), 1);
              };
            }
          };
        }
      };
    }]);

})();
