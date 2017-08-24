/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_viewer_directive');

  var module = angular.module('gn_viewer_directive', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('gnMainViewer', [
    'gnMap',
    'gnConfig',
    'gnSearchLocation',
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMeasure',
    'gnViewerService',
    function(gnMap, gnConfig, gnSearchLocation,
             gnSearchSettings, gnViewerSettings, gnMeasure,
             gnViewerService) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/components/viewer/' +
            'partials/mainviewer.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs['map']);
              scope.addLayerTabs = {
                search: true,
                wms: false,
                wmts: false,
                kml: false
              };

              /** these URL can be set by the viewer service **/
              scope.addLayerUrl = {
                wms: '',
                wmts: ''
              };

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** measure interaction */
              scope.mInteraction = gnMeasure.create(scope.map,
                  scope.measureObj, scope);

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** active tool selector */
              scope.activeTools = {
                addLayers: false,
                contexts: false,
                filter: false,
                layers: false,
                print: false,
                processes: false
              };

              /** optional tabs **/
              scope.disabledTools = gnViewerSettings.mapConfig.disabledTools;

              /** wps process tabs */
              scope.wpsTabs = {
                byUrl: true,
                recent: false
              };
              scope.selectedWps = {};

              scope.zoom = function(map, delta) {
                gnMap.zoom(map, delta);
              };
              scope.zoomToMaxExtent = function(map) {
                map.getView().fit(map.getView().
                    getProjection().getExtent(), map.getSize());
              };
              scope.ol3d = null;


              // 3D mode is allowed and disabled by default
              scope.is3DModeAllowed =
                  gnViewerSettings.mapConfig.is3DModeAllowed || false;
              scope.is3dEnabled = gnConfig['is3dEnabled'] || false;

              // By default, synch only background layer
              // between main map and search map
              scope.synAllLayers = false;

              scope.map.getLayers().on('add', function() {
                if (angular.isDefined(gnSearchSettings.searchMap))
                  scope.doSync(map);
              });

              scope.map.getLayers().on('change:length', function() {
                if (angular.isDefined(gnSearchSettings.searchMap)) {
                  scope.doSync(map);
                }
              });

              scope.syncMod = function() {
                scope.synAllLayers = !scope.synAllLayers;
                scope.doSync();
              };

              scope.doSync = function() {
                if (scope.synAllLayer) {
                  var layers = gnSearchSettings.searchMap.getLayers();
                  layers.clear();
                  scope.map.getLayers().forEach(function(l) {
                    layers.push(l);
                  });
                }
              };

              scope.init3dMode = function(map) {
                if (map) {
                  scope.ol3d = new olcs.OLCesium({map: map});
                  scope.ol3d.enableAutoRenderLoop();
                } else {
                  console.warning('3D mode can be only by activated' +
                      ' on a map instance.');
                }
              };
              scope.switch2D3D = function(map) {
                if (scope.ol3d === null) {
                  scope.init3dMode(map);
                }
                scope.ol3d.setEnabled(
                    scope.is3dEnabled = !scope.ol3d.getEnabled());
              };
              // Turn off 3D mode when not using it because
              // it slow down the application.
              // TODO: improve
              scope.$on('$locationChangeStart', function() {
                if (!gnSearchLocation.isMap() && scope.is3dEnabled) {
                  scope.switch2D3D(scope.map);
                }
              });

              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

              // watch open tool specified by the service; this will allow code
              // from anywhere to interact with the viewer tabs
              // note: this uses a deep equality to check the tool properties
              scope.$watch(function() {
                return gnViewerService.getOpenedTool();
              }, function(openedTool) {
                // open the correct tool using gi-btn magic
                switch (openedTool.name.toLowerCase()) {
                  case 'addlayers':
                    scope.activeTools.addLayers = true; break;
                  case 'contexts':
                    scope.activeTools.contexts = true; break;
                  case 'filter':
                    scope.activeTools.filter = true; break;
                  case 'layers':
                    scope.activeTools.layers = true; break;
                  case 'print':
                    scope.activeTools.print = true; break;
                  case 'processes':
                    scope.activeTools.processes = true; break;
                  case 'measure':
                    scope.mInteraction.active = true; break;
                  case 'annotations':
                    scope.drawVector.active = true; break;
                }

                // handle addlayers tab & url
                if (scope.activeTools.addLayers) {
                  switch (openedTool.tab) {
                    case 'wms':
                    case 'wmts':
                      scope.addLayerTabs[openedTool.tab] = true;
                      scope.addLayerUrl[openedTool.tab] = openedTool.url;
                      break;
                  }
                }

                // handle processes tool
                if (scope.activeTools.processes && openedTool.url) {
                  scope.wpsTabs.byUrl = true;
                  scope.selectedWps.url = openedTool.url;
                }
              }, true);

              // ogc graticule
              var ogcGraticule = gnViewerSettings.mapConfig.graticuleOgcService;
              if (ogcGraticule && ogcGraticule.layer && ogcGraticule.url) {
                scope.graticuleOgcService = ogcGraticule;
              }
            },
            post: function postLink(scope, iElement, iAttrs, controller) {
              //TODO: find another solution to render the map
              setTimeout(function() {
                scope.map.updateSize();
              }, 100);
            }
          };
        }
      };
    }]);

  // TODO : to remove those directives when ngeo allow null class
  module.directive('giBtnGroup', function() {
    return {
      restrict: 'A',
      controller: ['$scope', function($scope) {
        var buttonScopes = [];

        this.activate = function(btnScope) {
          angular.forEach(buttonScopes, function(b) {
            if (b != btnScope) {
              b.ngModelSet(b, false);
            }
          });
        };

        this.addButton = function(btnScope) {
          buttonScopes.push(btnScope);
        };
      }]
    };
  })
      .directive('giBtn', ['$parse', function($parse) {
        return {
          require: ['?^giBtnGroup', 'ngModel'],
          restrict: 'A',
          //replace: true,
          scope: true,
          link: function(scope, element, attrs, ctrls) {
            var buttonsCtrl = ctrls[0], ngModelCtrl = ctrls[1];
            var ngModelGet = $parse(attrs['ngModel']);
            var cls = attrs['giBtn'];
            scope.ngModelSet = ngModelGet.assign;

            if (buttonsCtrl) buttonsCtrl.addButton(scope);

            //ui->model
            element.bind('click', function() {
              scope.$apply(function() {
                ngModelCtrl.$setViewValue(!ngModelCtrl.$viewValue);
                ngModelCtrl.$render();
              });
            });

            //model -> UI
            ngModelCtrl.$render = function() {
              if (buttonsCtrl && ngModelCtrl.$viewValue) {
                buttonsCtrl.activate(scope);
              }
              if (cls != '') {
                element.toggleClass(cls, ngModelCtrl.$viewValue);
              }
            };
          }
        };
      }]);

  /**
   * @ngdoc directive
   * @name gn_viewer_directive:gnvToolsBtn
   * @deprecated Use giBtn and giBtnGroup instead
   */
  module.directive('gnvToolsBtn', [
    function() {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          element.bind('click', function() {
            if (element.hasClass('active')) {
              element.removeClass('active');
              $(element.attr('rel')).addClass('force-hide');
            } else {
              $('.btn').removeClass('active');
              element.addClass('active');
              $('.panel-tools').addClass('force-hide');
              $(element.attr('rel')).removeClass('force-hide');
            }
          });
        }
      };
    }]);

  module.directive('gnvLayerIndicator', ['gnWmsQueue',
    function(gnWmsQueue) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/' +
            'partials/layerindicator.html',
        link: function(scope, element, attrs) {
          scope.layerQueue = gnWmsQueue;
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer_directive:gnvClosePanel
   * @deprecated Use giBtn and giBtnGroup instead
   */
  module.directive('gnvClosePanel', [
    function() {
      return {
        restrict: 'A',
        require: 'giBtnGroup',
        scope: true,
        link: function(scope, element, attrs, btngroupCtrl) {
          $('.close').click(function() {
            var t = $(this).parents('.panel-tools');
            t.addClass('force-hide');
            $('[rel=#' + t.attr('id') + ']').removeClass('active');
            scope.$apply(function() {
              btngroupCtrl.activate();
            });
          });
        }
      };
    }]);
})();
