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
  module.directive('sxtPanier', [ 'sxtPanierService',
    function(sxtPanierService) {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          panier: '=sxtPanier',
          user: '=sxtPanierUser'
        },
        templateUrl: '../../catalog/views/sextant/panier/' +
            'partials/panier.html',
        controller: ['$scope', function($scope) {
          this.del = function(md) {
            $scope.panier.splice($scope.panier.indexOf(md), 1);
          };
        }],
        link: function(scope, element, attrs, controller) {

          scope.formObj = {
            user: {
              lastname: scope.user.name,
              firstname: scope.user.surname,
              mail: angular.isArray(scope.user.email) && scope.user.email[0],
              organisation: scope.user.organisation
            },
            layers: []
          };
          scope.extract = function() {
            sxtPanierService.extract(scope.formObj).then(function(data) {
              element.find('.modal').modal('hide');
              if(data.data.success) {
                scope.report = {
                  success: true
                };
                scope.panier = [];
              } else {
                scope.report = {
                  success: false
                };
              }
            });
            //scope.panier = [];
          };

          scope.downloadDisabled = true;
          scope.validateDownload = function() {
            var enable = true;
            $.each(scope.panier, function(i, elt) {
              enable = enable && elt.validated;
            });
            scope.downloadDisabled = !enable;
          }

          scope.goBack = function() {
            window.history.back();
          };

          scope.resetReport = function() {
            scope.report = undefined;
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
          md: '=sxtPanierEltMd',
          link: '=sxtPanierEltLink'
        },
        templateUrl: '../../catalog/views/sextant/panier/' +
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
                id: scope.md.getUuid(),
                input: {
                  format: gnPanierSettings.defaults.format,
                  epsg: gnPanierSettings.defaults.proj,
                  protocol: scope.link.protocol,
                  linkage: scope.link.url
                },
                output: {
                  format: gnPanierSettings.defaults.format,
                  epsg: gnPanierSettings.defaults.proj,
                  name: scope.link.name
                }
              };

              /** Map useed to draw the bbox */
              scope.map = new ol.Map({
                layers: [
                  new ol.layer.Tile({
                    source: new ol.source.OSM()
                  })
                ],
                controls:[],
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

              scope.formObj.layers.push(scope.form);

              scope.del = function(md, form) {
                controller.del(md);
                scope.formObj.layers.splice(
                    scope.formObj.layers.indexOf(form), 1);
              };

              var format = new ol.format.WKT();
              scope.$watch('extent', function(n) {
                if(n) {
                  try {
                    var g = format.readGeometry(n);
                    var e = g.getExtent();
                    angular.extend(scope.form.output, {
                      xmin: e[0].toString(),
                      ymin: e[1].toString(),
                      xmax: e[2].toString(),
                      ymax: e[3].toString(),
                      mercator_lat: ''
                    })
                  }
                  catch(e) {}
                }
              });
            }
          };
        }
      };
    }]);

})();
