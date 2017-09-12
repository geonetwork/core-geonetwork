(function() {
  goog.provide('sxt_panier_directive');

  goog.require('gn_popup');

  var module = angular.module('sxt_panier_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name sxt_panier_directive.directive:sxtPanier
   *
   * @description
   */
  module.directive('sxtPanier', [
    'sxtPanierService',
    'gnSearchLocation',
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

          var modal = element.find('.modal');
          $('.g').append(element.find('.modal'));
          modal.on('shown.bs.modal', function() {
            $('.g').append(modal.data('bs.modal').$backdrop);
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

          // because user loading is asynch
          var unregisterUserWatch = scope.$watch('user', function(user) {
            if(user && user.surname) {
              scope.prefillUser = !user.generic;
              if(scope.prefillUser) {
                angular.extend(scope.formObj.user, {
                  lastname: user.surname,
                  firstname: user.name,
                  mail: angular.isArray(user.email) ?
                      user.email[0] : user.email,
                  org: (angular.isArray(user.organisation) ?
                      user.organisation[0] : user.organisation) || ''
                });
              }
              unregisterUserWatch();
            }
          }, true);

          scope.extract = function() {
            sxtPanierService.extract(scope.formObj).then(function(data) {
              modal.modal('hide');
              if(data.data.success) {
                scope.downloadDisabled = true;
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
    'gnPopup',
    'gnWpsService',
    function(gnMap,
      gnSearchSettings,
      gnPanierSettings,
      gnPopup,
      gnWpsService) {
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

              scope.isCopyfile = scope.element.link.protocol == 'COPYFILE';

              /** Use to know if we need to zoom on the md extent or not */
              var rendered = false;

              /** object that contains the form values */
              var inputCrs, crs = angular.isArray(scope.element.md.crs) ?
                  scope.element.md.crs[0] : scope.element.md.crs;

              var crsExp = crs && (/(?:EPSG:)(\d+)/.exec(crs));
              if(angular.isArray(crsExp)) {
                crs = crsExp[1];
              }
              else {
                crs = crs && crs.split('::')[crs.split('::').length-1];
              }

              if(crs) {
                for(var i=0;i<gnPanierSettings.projs.length;i++) {
                  var p = gnPanierSettings.projs[i];
                  if(p.value == crs) {
                    inputCrs = crs;
                    break;
                  }
                }
              }
              if(!inputCrs) {
                inputCrs = gnPanierSettings.projs[0].value;
              }


              var dataType = scope.element.md.spatialRepresentationType;
              dataType = dataType == 'grid' ? 'raster' : dataType;

              // To pass the extent into an object for scope issues
              scope.prop = {};
              scope.formats = dataType ? scope.settings.formats[dataType] :
                  scope.settings.formats['vector'].concat(
                      scope.settings.formats['raster']);

              scope.form = {
                id: scope.element.md.getUuid(),
                input: {
                  format: dataType,
                  epsg: inputCrs,
                  protocol: scope.element.link.protocol,
                  linkage: scope.element.link.url
                },
                output: {
                  format: scope.formats[0].value,
                  epsg: inputCrs,
                  name: scope.element.link.name
                },

                // this ref to the element will not be sent to the extractor
                // and is only used to add additional inputs to the form object
                _element: scope.element
              };

              if(!scope.isCopyfile) {

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
                  var featureOverlay = new ol.layer.Vector({
                    source: new ol.source.Vector({
                      useSpatialIndex: false
                    }),
                    style: gnSearchSettings.olStyles.mdExtent,
                    map: scope.map
                  });

                  featureOverlay.getSource().addFeature(feature);

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
                    scope.map.getView().fit(
                        feature.getGeometry().getExtent(), scope.map.getSize());
                  }
                  rendered = true;
                });
              }
              else {
                scope.form.input.epsg = '';
                scope.form.input.format= '';
                scope.form.output.epsg = '';
                scope.form.output.format= '';
              }

              // by default, include WFS filters if there are any
              scope.form.useFilters = true;
            },
            post: function preLink(scope, iElement, iAttrs, controller) {

              scope.formObj.layers.push(scope.form);

              scope.del = function(element, form) {
                controller.del(element);
                scope.formObj.layers.splice(
                    scope.formObj.layers.indexOf(form), 1);
              };

              var format = new ol.format.WKT();
              scope.$watch('element.filter.extent', function(e) {
                if(e) {
                  scope.prop.extent = format.writeGeometry(
                    ol.geom.Polygon.fromExtent(e));
                }
              })

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

              // Generate WPS form
              scope.currentWPS = null;
              scope.editWPSForm = function(process) {
                if(process) {
                  // open modal
                  var popup = gnPopup.create({
                    title: 'editWpsForm',
                    content:
                      '<gn-wps-process-form wps-link="currentWPS" '+
                        'wfs-link="wfsLink" map="map" ' +
                        'hide-execute-button="true">' +
                      '</gn-wps-process-form>' +
                      '<button type="button" class="btn btn-default" ' +
                        'ng-click="saveWPSMessage(); $parent.close()">' +
                        '{{ "wpsSaveForm" | translate }}' +
                      '</button>',
                    className: 'wps-form-modal',
                    onCloseCallback: function () {
                      scope.currentWPS = null;
                    }
                  }, scope);

                  scope.currentWPS = process;

                  // use WFS link
                  scope.wfsLink = scope.element.link.protocol == 'OGC:WFS' ?
                      scope.element.link : null;
                }
              };
              scope.saveWPSMessage = function() {
                var process = scope.currentWPS;

                // do a describe process on the WPS & save execute message
                gnWpsService.describeProcess(process.url, process.name).then(
                  function (data) {
                    // generate the XML message from the description
                    var description = data.processDescription[0];
                    var message = gnWpsService.printExecuteMessage(description,
                      process.inputs, process.output);

                    process.executeMessage = message;
                });

                // clear ref to process
                scope.currentWPS = null;
              };
            }
          };
        }
      };
    }]);

})();
