(function() {
  goog.provide('sxt_viewer_directive');

  var module = angular.module('sxt_viewer_directive', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('sxtMainViewer', [
    'gnMap',
    'gnSearchLocation',
    function(gnMap, gnSearchLocation) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/mapviewer/mainviewer.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs['map']);

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
                map.getView().setResolution(gnMapConfig.maxResolution);
              };

              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

              scope.active = {
                tool: false,
                layersTools: false,
                NCWMS: null
              };
              scope.locService = gnSearchLocation;

            },
            post: function postLink(scope, iElement, iAttrs, controller) {

              iElement.find('.panel-tools .btn-default.close').click(function() {
                scope.active.tool = false;
              });

              //TODO: find another solution to render the map
              setTimeout(function() {
                scope.map.updateSize();
              }, 100);

              scope.map.addControl(new ol.control.ScaleLine({
                target: document.querySelector('footer')
              }));

            }
          };
        }
      };
    }]);

  module.directive('sxtTool', [ function() {
    return {
      restrict: 'A',
      link: function (scope, element) {
        element.on('click', function() {
          scope.active.tool = !$(this).hasClass('active');
        });
      }
    }
  }]);

  module.directive('sxtCloseTool', [ function() {
    return {
      restrict: 'A',
      link: function (scope, element) {
        element.on('click', function() {
          scope.$apply(function() {
            scope.active.tool = false;
          });
        });
      }
    }
  }]);

  module.directive('sxtMousePosition', [ function() {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/views/sextant/templates/mouseposition/mouseposition.html',
      link: function (scope, element) {
        scope.projection = 'EPSG:4326';

        var control = new ol.control.MousePosition({
          projection: 'EPSG:4326',
          coordinateFormat: function(c) {
            return c.map(function(i) { return i.toFixed(3) });
          },
          target: element[0]
        });
        scope.map.addControl(control);
        scope.setProjection = function() {
          control.setProjection(ol.proj.get(scope.projection));
        }
      }
    }
  }]);

  module.directive('sxtFullScreen', [ function() {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        var map = scope.$eval(attrs['sxtFullScreen']);
        // FIXME: which element to maximize??
        var elem = $('[sxt-main-viewer]')[0];
        element.on('click', function() {
          if (!document.fullscreenElement && !document.mozFullScreenElement &&
            !document.webkitFullscreenElement) {
            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            } else if (elem.mozRequestFullScreen) {
                elem.mozRequestFullScreen();
            } else if (elem.webkitRequestFullscreen) {
                elem.webkitRequestFullscreen();
            }
          } else {
            if (document.cancelFullScreen) {
              document.cancelFullScreen();
            } else if (document.mozCancelFullScreen) {
              document.mozCancelFullScreen();
            } else if (document.webkitCancelFullScreen) {
              document.webkitCancelFullScreen();
            }
          }
        });
      }
    }
  }]);

  module.directive('sxtLayersTools', [ function() {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        element.find('.flux button').on('click', function(e) {
          var active = $(this).hasClass('active');
          var elem = $(this);
          scope.$apply(function(){
            scope.active.NCWMS = null;
            scope.active.layersTools = !active;
          });
          if (active) {
            elem.removeClass('active');
            e.stopImmediatePropagation();
          }
        });
      }
    }
  }]);


})();
