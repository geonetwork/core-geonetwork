(function() {
  goog.provide('sxt_viewer_directive');

  goog.require('GML_3_1_1');
  goog.require('OWS_1_1_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WPS_1_0_0');
  goog.require('XLink_1_0');

  // WPS Client
  // Jsonix wrapper to read or write WPS response or request
  var context = new Jsonix.Context(
    [XLink_1_0, OWS_1_1_0, WPS_1_0_0, GML_3_1_1, SMIL_2_0, SMIL_2_0_Language],
    {
      namespacePrefixes: {
        'http://www.w3.org/1999/xlink': 'xlink',
        'http://www.opengis.net/ows/1.1': 'ows',
        'http://www.opengis.net/wps/1.0.0': 'wps',
        'http://www.opengis.net/gml': 'gml'
      }
    }
  );
  var unmarshaller = context.createUnmarshaller();
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
              scope.drawVector = new ol.layer.Vector();

              /** print definition */
              scope.activeTools = {};

              if (!gnViewerSettings.menuCollapsed) {
                $timeout(function() {
                  $('[gi-btn][rel="#layers"]').click();
                });
              }

              var map = scope.map;
              $($window).on('resize',
                function() { setTimeout(map.updateSize.bind(map), 100) }
              );

              // this holds the current active OL layer (to display options such as WFS filters, etc.)
              scope.activeLayer = null;
              scope.setActiveLayer = function(layer) {
                scope.activeLayer = layer;
              }
              scope.clearActiveLayer = function() {
                scope.activeLayer = null;
              }
              scope.hasActiveLayer = function() {
                return !!scope.activeLayer;
              }

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

              // copy of the code in LocalisationDirective (TODO: put this in a service)
              scope.zoomToYou = function(map) {
                if (navigator.geolocation) {
                  navigator.geolocation.getCurrentPosition(function(pos) {
                    var position = new ol.geom.Point([
                      pos.coords.longitude,
                      pos.coords.latitude]);
                    map.getView().setCenter(
                      position.transform(
                        'EPSG:4326',
                        map.getView().getProjection()).getFirstCoordinate()
                    );
                  });
                }
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

              scope.active = {
                tool: false
              };

              scope.locService = gnSearchLocation;

              scope.isMenuVisible = function() {
                return !gnViewerSettings.menuHidden;
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

              // create measure interaction
              scope.mInteraction = gnMeasure.create(map,
                scope.measureObj, scope);

              // save processes from viewer settings
              scope.processes = gnViewerSettings.processes;
              scope.selectedProcess = scope.processes && scope.processes[0];

              scope.profileTool = gnViewerSettings.profileTool;

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
        };
      }
    };
  }]);
  /**
   * @ngdoc directive
   * @name gn_viewer.directive.sxtProfilTool:
   *
   * @description enables the draw a line and get the profil base on configuration
   * in applicationProfil
   */
  module.directive('sxtProfileTool', [
    'gnGeometryService',
    'gnWpsService',
    'gnProfileService',
    '$http',
    '$q',
    function (gnGeometryService, gnWpsService, gnProfileService, $http, $q) {
      return {
        restrict: 'A',
        scope: {
          map: '=sxtProfileTool',
          wpsLink: '='

        },
      link: function(scope, element, attrs) {

        var drawInteraction = new ol.interaction.Draw({
          type: 'LineString'
        });
        var drawing = false;
        element.hide();
        if (!scope.wpsLink || !scope.wpsLink.url || !scope.wpsLink.name) {
          console.warn('No wps process for profile tool found in sxtSettings');
        }
        var processUri = scope.wpsLink.url || undefined;
        var processId = scope.wpsLink.name || undefined;
        var mapProjection = scope.map.getView().getProjection().getCode();
        var inputs = scope.wpsLink.applicationProfile.inputs;
        var description;
        var output = {
          asReference: false,
          identifier: "output_json",
          lineage: false,
          mimeType: "application/json",
          status: false,
          storeExecuteResponse: false
        };

        // call describeProcess
        gnWpsService.describeProcess(processUri, processId).then(
          function (data) {
            // generate the XML message from the description
            description = data.processDescription[0];
            element.show();
            return description;
          });
        // this is the temp layer for keeping the profil on the map
        // during the WPS process time
        var style = new ol.style.Style({
          text: new ol.style.Text({
            font: 'bold 11px "Open Sans", "Arial Unicode MS", "sans-serif"',
            placement: 'line'
          })
        });
        var layer = new ol.layer.Vector({
          source: new ol.source.Vector({
            useSpatialIndex: true,
            features: new ol.Collection()
          }),
          style: function(f) {
            style.getText().setText('Calcul en cours...');
            return style;
          }
        });

        element.on('click', function() {
          element.toggleClass('active');
          if (!drawing) {
            drawing = true;
            scope.map.addInteraction(drawInteraction);
            scope.map.addLayer(layer);
          } else {
            drawing = false;
            scope.map.removeInteraction(drawInteraction);
            scope.map.removeLayer(layer);

          }
        });
        // clear existing features on draw end & save feature
        drawInteraction.on('drawend', function(event) {
          layer.getSource().addFeature(event.feature);
          var geometryOutput = gnGeometryService.printGeometryOutput(scope.map, event.feature, {
            format: 'gml',
            crs: mapProjection,
            outputAsWFSFeaturesCollection: "true"
          });
          // remove previous vlayer input
          inputs = inputs.filter(function (e) {
            return e.name !== "vlayer";
          });
          inputs.push({
              "name": "vlayer",
              "value": geometryOutput
          });
          var message = gnWpsService.printExecuteMessage(description, inputs, output);
          processResponse = function(response) {
            var errorMessage = "Something went wrong in WPS execute process"
            if (response.TYPE_NAME === 'WPS_1_0_0.ExecuteResponse') {
              if (response.status !== undefined) {
                if (response.status.processSucceeded !== undefined ||
                  response.status.processFailed !== undefined) {
                  if (!response.status.processSucceeded) {
                    console.error(errorMessage);
                  }
                }
              }
            }
            else {
              console.error(errorMessage);
            }

            // save raw graph data on view controller & hide it in wps form
            if (response.processOutputs) {
              output.asReference = false;
              try {
                var jsonData = JSON.parse(response.processOutputs.output[0].data.complexData.content);
                gnProfileService.displayProfileGraph(
                  jsonData.profile,
                  {
                    valuesProperty: 'values',
                    xProperty: 'lon',
                    yProperty: 'lat',
                    distanceProperty: 'dist',
                    crs: 'EPSG:4326'
                  }
                );
              } catch (e) {
                console.error('Error parsing WPS graph data:',
                  response.processOutputs);
              }
            }
          };
          var defer = $q.defer();
          $http.post(processUri, message, {
            headers: {'Content-Type': 'application/xml'}
          }).then(function(data) {
            var response =
              unmarshaller.unmarshalString(data.data).value;
            defer.resolve(response);
            layer.getSource().removeFeature(event.feature);
            processResponse(response);
          }, function(data) {
            defer.reject(data);
            layer.getSource().removeFeature(event.feature);
          });
        });
      }
    };
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
