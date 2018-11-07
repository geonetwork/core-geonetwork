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
    '$window',
    '$timeout',
    'gnMap',
    'gnSearchLocation',
    'gnConfig',
    'gnViewerSettings',
    '$translate',
    'gnMeasure',
    function($window, $timeout, gnMap, gnSearchLocation, gnConfig,
      gnViewerSettings, $translate, gnMeasure) {
      return {
        restrict: 'A',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/mapviewer/mainviewer.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs['map']);

              /** these URL can be set by the viewer service **/
              scope.addLayerUrl = {
                wms: '',
                wmts: ''
              }

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** print definition */
              scope.activeTools = {};
              if (gnViewerSettings.menuExpanded) {
                $timeout(function() {
                  $('[gi-btn][rel="#layers"]').click();
                });
              }

              var map = scope.map;
              $($window).on('resize',
                function() { setTimeout(map.updateSize.bind(map), 100) }
              );

              var firstRun = true;
              scope.handleTabParsing = function() {
                if (!firstRun) { return; }
                firstRun = false;
                scope.layerTabs.legend.active = false;
              };

              scope.closeLayerTab = function() {
                for(var p in scope.layerTabs) {
                  if (scope.layerTabs[p].active) {
                    scope.layerTabSelect(p);
                  }
                }
              };
              scope.isLayerTabActive = function() {
                for(var p in scope.layerTabs) {
                  if (scope.layerTabs[p].active) return true;
                }
                return false;
              };

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

              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

              var separatedTools = ['ncwms', 'wps', 'wfsfilter'];

              scope.active = {
                tool: false,
                layersTools: false
              };
              separatedTools.forEach(function(tool) {
                scope.active[tool.toUpperCase()] = null;
              });

              scope.locService = gnSearchLocation;

              var activeTab = null;
              scope.layerTabSelect = function(tab) {
                if (!scope.active.layersTools) {
                  scope.active.layersTools = true;
                  scope.layerTabs[tab].active = true;
                  activeTab = tab;
                } else if (tab == activeTab) {
                  scope.active.layersTools = false;
                  scope.active.maximized = false;
                  scope.layerTabs[tab].active = false;
                  activeTab = null;
                  iElement.find('.main-tools').removeClass('sxt-maximize-layer-tools');
                } else {
                  scope.layerTabs[tab].active = true;
                  activeTab = tab;
                }
              };

              scope.isContainerOpened = function() {
                try {
                  return scope.activeTools.layers || scope.activeTools.import ||
                    scope.activeTools.contexts || scope.activeTools.print ||
                    scope.mInteraction.active || scope.drawVector.active ||
                    scope.activeTools.benthique || scope.activeTools.processes;
                }
                catch (e) {}
                return false;
              };

              scope.loadTool = function(tab, layer) {
                  scope.layerTabs[tab].active = true;
                  scope.active.layersTools = true;
                  scope.active[tab.toUpperCase()] = layer;
                  activeTab = tab;
              };


              /**
               * Tells if a separated tool (everything but legend/sources/sort)
               * is opened in the tool panel.
               * @returns {boolean}
               */
              scope.isSeparatedTool = function() {
                return separatedTools.indexOf(activeTab) >= 0;
              };

              scope.map.getLayers().on('remove', function(e) {
                separatedTools.forEach(function(tool) {

                  var toolUpper = tool.toUpperCase();
                  if(scope.active[toolUpper] &&
                      scope.active[toolUpper] == e.element) {
                    scope.active[toolUpper] = null;
                    scope.active.layersTools = false;
                    scope.layerTabs[tool].active = false;
                    activeTab = null;
                  }
                });
                if (e.element.get('wfsfilter-el')) {
                  e.element.get('wfsfilter-el').remove();
                }
                if (e.element.get('wpsfilter-el')) {
                  e.element.get('wpsfilter-el').remove();
                }
              });

              // create measure interaction
              scope.mInteraction = gnMeasure.create(map,
                scope.measureObj, scope);


              // save processes from viewer settings
              scope.processes = gnViewerSettings.processes;
              scope.selectedProcess = scope.processes && scope.processes[0];

              // selects a process
              scope.selectProcess = function (p) {
                // show panel & hide others
                  scope.activeTools.processes = true;

                // select process (used by gnWpsProcessForm directive)
                scope.selectedProcess = p;
              };

              // outputs a label based on process info
              scope.getProcessLabel = function (p) {
                var currentLang = $translate.use();
                if (p.labels) {
                  return p.labels[currentLang];
                } else if (p.label) {
                  return p.label;
                } else {
                  return p.name;
                }
              }

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

              scope.map.addControl(new ol.control.ScaleLine({
                target: document.querySelector('footer')
              }));


              scope.isNcwms = function(layer) {
                return layer.ncInfo;
              };

              scope.hasGeoSearch = !!gnViewerSettings.localisations;


            }
          };
        }
      };
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
        $(document).on(
          'mozfullscreenchange webkitfullscreenchange fullscreenchange',
          function() { setTimeout(map.updateSize.bind(map), 100) }
        );
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

  module.directive('sxtPopoverDropdown', [ '$timeout', function($timeout) {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        var content = element.find('ul').css('display', 'none');
        var button = element.find('.dropdown-toggle');
        if (attrs['container']) {
          var grid = element.parents(attrs['container']).first();
        }

        $timeout(function() {
          var className = (attrs['fixedHeight'] != 'false')  ?
            'popover-dropdown popover-dropdown-'+content.find('li').length : '' ;
          if (attrs['classname']) {
            className += ' ' + attrs['classname'];
          }
          var title = button[0].title;
          button.popover({
            animation: false,
            container: (grid) ? grid[0] : '[sxt-main-viewer]',
            placement: attrs['placement'] || 'right',
            content: ' ',
            template: '<div class="bottom popover ' + className + '">' +
            '<div class="arrow"></div><div class="popover-content"></div></div>'
          });

          // required because bootstrap popover clears the title attribute...
          button[0].title = title;
        }, 1, false);

        button.on('shown.bs.popover', function() {
          var $tip = button.data('bs.popover').$tip;
          content.css('display', 'inline').appendTo(
            $tip.find('.popover-content')
          );
          if (grid && grid.length > 0) {
            $tip.find('.arrow').css({
              left: $(button).offset().left - grid.offset().left + $(button).parent().width()/2
            });
            $tip.css({
              right: '0',
              left: '0'
            });
          }
        });
        button.on('hidden.bs.popover', function() {
          content.css('display', 'none').appendTo(element);
        });

        var hidePopover = function() {
          button.popover('hide');
          if (button.data('bs.popover').inState) {
            button.data('bs.popover').inState.click = false;
          }
        };
        scope.hidePopover = hidePopover;

        // can’t use dismiss boostrap option: incompatible with opacity slider
        var onMousedown = function(e) {
          if (
            (button.data('bs.popover') && button.data('bs.popover').$tip)
            && (button[0] != e.target)
            && (!$.contains(button[0], e.target))
            && (button.data('bs.popover').$tip.find('.dropdown-menu')[0] !=
              e.target)
            && ((!grid && ($(e.target).parents('.popover')[0] !=
              button.data('bs.popover').$tip[0])) || grid)
          ) {
            var timeout = (grid && ($(e.target).parents('.popover')[0] ==
            button.data('bs.popover').$tip[0])) ? 200 : 30;
            $timeout(hidePopover, timeout, false);
          }
        };
        $('body').on('mousedown click', onMousedown);

        if (attrs['sxtPopoverDismiss']) {
          $(attrs['sxtPopoverDismiss']).on('scroll', hidePopover);
        }

        element.on('$destroy', function() {
          $('body').off('mousedown click', onMousedown);
          if (attrs['sxtPopoverDismiss']) {
            $(attrs['sxtPopoverDismiss']).off('scroll', hidePopover);
          }

        });
      }
    }
  }]);

  module.directive('sxtSelect', [ '$timeout', function($timeout) {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        $timeout(function(){
          element.selectpicker();
        }, 0);
      }
    }
  }]);

  module.directive('gnvLayermanagerBtn', ['$timeout',
    function($timeout) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {         
          var toggleLayer = attrs['gnvLayermanagerBtn'] === 'true';
          var buttons = element.find('.btn-group.flux button');
          var container = element.find('.panel-carousel-container');

          container.on('focus', 'input[type="text"]', function(event) {
            buttons.removeClass('active');
            buttons.eq($(event.target.closest('.panel-carousel')).index()).addClass('active');
          });

          buttons.bind('click', function () {
            buttons.removeClass('active');
            element.addClass('active');
            $(this).addClass('active');
            if (toggleLayer) element.find('.layers').addClass('collapsed');
            element.find('.panel-carousel').removeClass('collapsed');
            element.find('.unfold').css('opacity', 1);
            element.find('.panel-carousel-container').css('left',
              '-' + ($(this).index() * 100) + '%');
          });

          element.find('.unfold').click(function() {
            element.find('.btn-group button').removeClass('active');
            if (toggleLayer) element.find('.layers').removeClass('collapsed');
            element.find('.panel-carousel').addClass('collapsed');
            element.find('.unfold').css('opacity', 0);
          });

          // Select the first menu when loaded
          $timeout(function() {
            if (buttons.get(0)) {
              buttons.get(0).click();
            }
          });
        }
      };
    }]);

})();
