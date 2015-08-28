(function() {
  goog.provide('gn_viewer_directive');

  goog.require('gn_gfi_directive');

  var module = angular.module('gn_viewer_directive', [
    'gn_gfi_directive', 'gfiFilters'
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('gnMainViewer', [
    'gnMap', 'gnConfig', 'gnSearchLocation',
    function(gnMap, gnConfig, gnSearchLocation) {
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

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** print definition */
              scope.activeTools = {};

              scope.zoom = function(map, delta) {
                gnMap.zoom(map, delta);
              };
              scope.zoomToMaxExtent = function(map) {
                map.getView().fit(map.getView().
                    getProjection().getExtent(), map.getSize());
              };
              scope.ol3d = null;

              // 3D mode is allowed and disabled by default
              scope.is3DModeAllowed = gnConfig['map.is3DModeAllowed'] || false;
              scope.is3dEnabled = gnConfig['is3dEnabled'] || false;



              scope.init3dMode = function(map) {
                if (map) {
                  scope.ol3d = new olcs.OLCesium({map: map});
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




              scope.zoomToYou = function(map) {
                if (navigator.geolocation) {
                  navigator.geolocation.getCurrentPosition(function(position) {
                    var position = new ol.geom.Point([
                      position.coords.longitude,
                      position.coords.latitude]);
                    map.getView().setCenter(
                        position.transform(
                        'EPSG:4326',
                        map.getView().getProjection()).getFirstCoordinate()
                    );
                  });
                } else {

                }
              };
              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

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
