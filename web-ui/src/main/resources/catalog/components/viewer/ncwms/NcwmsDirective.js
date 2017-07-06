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
  goog.provide('gn_ncwms_directive');

  var module = angular.module('gn_ncwms_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnNcwmsTransect
   *
   * @description
   * If we detects in the capabilities that the layer comes from NCWMS, then
   * we add some properties to it and we display in the layermanager item
   * a additional list of tools for this specific layer.
   * The directive `gnNcwmsTransect` provides the form for all NCWMS parameters.
   */

  var DATE_INPUT_FORMAT = 'DD-MM-YYYY';

  module.directive('gnNcwmsTransect', [
    'gnHttp',
    'gnNcWms',
    'gnPopup',
    function(gnHttp, gnNcWms, gnPopup) {
      return {
        restrict: 'A',
        scope: {
          layer: '=',
          map: '='
        },
        templateUrl: '../../catalog/components/viewer/ncwms/' +
            'partials/ncwmstools.html',
        link: function(scope, element, attrs) {

          var drawInteraction, featureOverlay;
          var map = scope.map;
          var isOceanotron = false;
          var elevationMin = 0;
          var elevationMax = 1;


          scope.ctrl = {};

          //element.find('[ui-slider]').slider();
          /**
           * Just manage active button in ui.
           * Values of activeTool can be 'time', 'profile', 'transect'
           * @param {string} activeTool
           */
          scope.setActiveTool = function(activeTool) {
            if (scope.activeTool == activeTool) {
              scope.activeTool = undefined;
              resetInteraction();
            } else {
              scope.activeTool = activeTool;
              activateInteraction(scope.activeTool);
            }
          };

          var resetInteraction = function() {
            if (featureOverlay) {
              featureOverlay.setMap(null);
              delete featureOverlay;
            }
            if (drawInteraction) {
              scope.map.removeInteraction(drawInteraction);
              delete drawInteraction;
            }
          };

          var activateInteraction = function(activeTool) {
            var type = 'Point';
            if (activeTool == 'transect') {
              type = 'LineString';
            }

            if (!featureOverlay) {
              featureOverlay = new ol.layer.Vector({
                source: new ol.source.Vector({
                  useSpatialIndex: false
                }),
                map: scope.map
              });
            }
            if (drawInteraction) {
              scope.map.removeInteraction(drawInteraction);
            }

            drawInteraction = new ol.interaction.Draw({
              features: featureOverlay.getSource().getFeaturesCollection(),
              type: type
            });
            drawInteraction.on('drawstart', function(evt) {
              featureOverlay.getSource().clear();
            });

            drawInteraction.on('drawend',
                function(evt) {

                  var url;
                  var coordinates = evt.feature.getGeometry().getCoordinates();
                  if (activeTool == 'time') {
                    url = scope.layer.getSource().getGetFeatureInfoUrl(
                        coordinates,
                        map.getView().getResolution(),
                        map.getView().getProjection(), {
                          TIME: gnNcWms.formatTimeSeries(
                              scope.timeSeries.tsfromD,
                              scope.timeSeries.tstoD),
                          //'2009-11-02T00:00:00.000Z/2009-11-09T00:00:00.000Z
                          //'2009-11-02T00:00:00.000Z/2009-11-09T00:00:00.000Z
                          INFO_FORMAT: 'image/png'
                        });
                  } else {

                    if(isOceanotron) {
                      var loader = new GnFeaturesGFILoader({
                        layer: scope.layer,
                        coordinates: coordinates,
                        map: scope.map
                      });
                      loader.loadAll().then(function(data) {
                        //TODO: extract feature id

                      });
                    }
                    url = gnNcWms.getNcwmsServiceUrl(
                        scope.layer,
                        scope.map.getView().getProjection(),
                        coordinates,
                        activeTool);
                  }

                  gnPopup.create({
                    title: activeTool,
                    url: url,
                    content: '<div class="gn-popup-iframe ' +
                        activeTool + '">' +
                        '<img style="width:100%;height:100%;" ' +
                        'src="{{options.url}}" />' +
                        '</div>'
                  });
                  scope.$apply(function() {
                    scope.activeTool = undefined;
                  });
                  setTimeout(function() {
                    resetInteraction();
                  }, 300);
                }, this);

            map.addInteraction(drawInteraction);

          };
          var disableInteractionWatchFn = function(nv, ov) {
            if (!nv) {
              resetInteraction();
              scope.activeTool = undefined;
            }
          };
          scope.$watch('layer.showInfo', disableInteractionWatchFn);
          scope.$watch('layer.visible', disableInteractionWatchFn);
          scope.$watch('layer', disableInteractionWatchFn);

          /**
           * init source layer params object
           */
          var initNcwmsParams = function() {

            var layer = scope.layer;
            var ncInfo = layer.ncInfo;

            isOceanotron = !!ncInfo.multiFeature;
            scope.ctrl.isOceanotron = isOceanotron;

            var proj = map.getView().getProjection();
            var bbox = [
              parseFloat(ncInfo.bbox[0]),
              parseFloat(ncInfo.bbox[1]),
              parseFloat(ncInfo.bbox[2]),
              parseFloat(ncInfo.bbox[3])
            ];

            // use bbox only if it is contained in the world extent
            layer.set('cextent',
              ol.extent.containsExtent(proj.getWorldExtent(), bbox) ?
                ol.proj.transformExtent(bbox, 'EPSG:4326', proj.getCode()) :
                proj.getExtent()
            );

            scope.params = layer.getSource().getParams() || {};
            scope.colorRange = {
              step: 1,
              min: ncInfo.scaleRange[0],
              max: ncInfo.scaleRange[1]
            };
            scope.colorscalerange = [scope.colorRange.min,
              scope.colorRange.max];
            scope.timeSeries = {
              from: undefined,
              to: undefined
            };
            scope.elevations = ncInfo.zaxis ? ncInfo.zaxis.values : [];
            scope.palettes = gnNcWms.parseStyles(ncInfo);

            if (angular.isUndefined(scope.params.LOGSCALE)) {
              scope.params.LOGSCALE = false;
            }

            // Set default STYLES= to WMS
            if(isOceanotron) {
              scope.layer.set('oceanotron', true);
              scope.ctrl.palette = ncInfo.defaultPalette || ncInfo.palettes[0];

              scope.ctrl.elevationMinFn = function(elev) {
                if(elev) {
                  scope.params.ELEVATION = elev + '/' + elevationMax;
                  scope.updateLayerParams();
                }
                return angular.isDefined(elev) ?
                  (elevationMin = elev) : elevationMin;
              };
              scope.ctrl.elevationMaxFn = function(elev) {
                if(elev) {
                  scope.params.ELEVATION = elevationMin + '/' + elev;
                  scope.updateLayerParams();
                }
                return angular.isDefined(elev) ?
                  (elevationMax = elev) : elevationMax;
              };
              scope.params.ELEVATION = elevationMin + '/' + elevationMax;

              // Init mendatory time range with day and day before
              var day = new Date();
              day.setDate(day.getDate() - 5);
              var to = moment(day).format(DATE_INPUT_FORMAT);
              day.setDate(day.getDate() - 1);
              var from = moment(day).format(DATE_INPUT_FORMAT);

              scope.ncTime.value = {
                from: from,
                to: to
              };

              scope.updateStyle();
            }
          };

          /**
           *  Get bounds of color range depending on the current extent.
           *  Called when user wlick on 'auto' button.
           *  Update the slider values to this bounds.
           */
          scope.setAutoColorranges = function(evt) {
            $(evt.target).addClass('fa-spinner');
            gnNcWms.getColorRangesBounds(scope.layer,
                ol.proj.transformExtent(
                map.getView().calculateExtent(map.getSize()),
                map.getView().getProjection(), 'EPSG:4326').join(',')).
                success(function(data) {
                  scope.colorscalerange = [data.min, data.max];
                  scope.onColorscaleChange(scope.colorscalerange);
                  $(evt.target).removeClass('fa-spinner');
                });
          };

          /**
           * Call when the input of the double slider get change.
           * The input is an array of 2 values. It updates the layer
           * with `COLORSCALERANGE` params and refreshes it.
           * @param {?array} v the colorange array
           */
          scope.onColorscaleChange = function(v) {
            if (angular.isArray(v) && v.length == 2) {
              colorange = v[0] + ',' + v[1];
              scope.params.COLORSCALERANGE = colorange;
              scope.updateLayerParams();
            }
          };

          scope.ncTime = {};

          scope.$watch('ncTime.value', function(time) {
            if (time) {
              var timeA = [];
              if(angular.isString(time)) {
                timeA.push(time);
              }
              else if(time.from && time.to) {
                timeA.push(time.from, time.to);
              }

              if(timeA.length) {
                scope.params.TIME = timeA.map(function(t){
                  return moment(t, 'DD-MM-YYYY').format(
                    'YYYY-MM-DD[T]HH:mm:ss.SSS[Z]')
                }).join('/');
                scope.updateLayerParams();
              }
            }
          }, true);

          scope.hasStyles = function() {
            try {
              return Object.keys(scope.palettes).length > 1;
            }
            catch(e) {
              return false;
            }
          };

          scope.updateStyle = function() {
            scope.params.STYLES = scope.palettes[scope.ctrl.palette];
            scope.updateLayerParams();
          };

          scope.updateLayerParams = function() {
            scope.layer.getSource().updateParams(scope.params);

            scope.layer.set('legend',
                gnNcWms.updateLengendUrl(scope.layer.get('legend'),
                    angular.extend({
                      PALETTE: scope.ctrl.palette
                    }, scope.params)));
          };

          element.bind('$destroy', function(e) {
            element.find('[ui-slider]').slider();
          });

          initNcwmsParams();
        }
      };
    }]);
})();
