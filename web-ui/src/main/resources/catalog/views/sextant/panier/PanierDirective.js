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
  module.directive('sxtPanier', [ 'sxtPanierService', 'gnSearchLocation',
    function(sxtPanierService, gnSearchLocation) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/panier/' +
            'partials/panier.html',
        controller: ['$scope', function($scope) {
          this.del = function(element) {
            $scope.panier.splice($scope.panier.indexOf(element), 1);
          };
        }],
        link: function(scope, element, attrs, controller) {

          scope.$watch('searchObj.panier', function(v) {
            scope.panier = v;
          });

          scope.locService = gnSearchLocation;

          scope.formObj = {
            user: {
              lastname: '',
              firstname: '',
              mail: '',
              org: ''
            },
            layers: []
          };
          
          scope.$watch('user', function(user) {
            if(user) {
              angular.extend(scope.formObj.user, {
                lastname: user.name,
                firstname: user.surname,
                mail: angular.isArray(user.email) ?
                    user.email[0] : user.email,
                org: user.organisation
              });
            }
          });

          scope.extract = function() {
            sxtPanierService.extract(scope.formObj).then(function(data) {
              element.find('.modal').modal('hide');
              if(data.data.success) {
                scope.report = {
                  success: true
                };
                scope.searchObj.panier = [];
                scope.formObj.layers = [];
              } else {
                scope.report = {
                  success: false
                };
              }
            });
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

          scope.$on('renderPanierMap', function() {
            scope.resetReport();
          });

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
          element: '=sxtPanierElt',
          formObj: '=sxtPanierEltForm'
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
              var crs = angular.isArray(scope.element.md.crs) ?
                  scope.element.md.crs[0] : scope.element.md.crs;
              crs = crs && crs.split('::')[crs.split('::').length-1];

              var dataType = scope.element.md.spatialRepresentationType_text ==
                  'Vecteur' ? 'vector' : 'raster';

              // To pass the extent into an object for scope issues
              scope.prop = {};
              scope.formats = scope.settings.formats[dataType];

              scope.form = {
                id: scope.element.md.getUuid(),
                input: {
                  format: angular.isArray(scope.element.md.format) ?
                      scope.element.md.format[0] :
                      scope.formats[0].value,
                  epsg: crs || gnPanierSettings.projs[0].value,
                  protocol: scope.element.link.protocol,
                  linkage: scope.element.link.url
                },
                output: {
                  format: angular.isArray(scope.element.md.format) ?
                      scope.element.md.format[0] :
                      scope.formats[0].value,
                  epsg: crs || gnPanierSettings.projs[0].value,
                  name: scope.element.link.name
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
              var extents = gnMap.getBboxFromMd(scope.element.md);
              var extent = extents.length > 0 && extents[0];
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

              scope.del = function(element, form) {
                controller.del(element);
                scope.formObj.layers.splice(
                    scope.formObj.layers.indexOf(form), 1);
              };

              var format = new ol.format.WKT();
              scope.$watch('prop.extent', function(n) {
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
