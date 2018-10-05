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
   * If we detect in the capabilities that the layer comes from NCWMS, then
   * we add some properties to it and we display in the layermanager item
   * a additional list of tools for this specific layer.
   * The directive `gnNcwmsTransect` provides the form for all NCWMS parameters.
   */

  module.directive('gnNcwmsTransect', [
    'gnHttp',
    'gnNcWms',
    'gnPopup',
    '$http',
    '$q',
    'ngeoDecorateInteraction',
    function(gnHttp, gnNcWms, gnPopup, $http, $q, ngeoDecorateInteraction) {
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
                var promiseUrl = gnNcWms.getResultImageUrl(scope.layer,
                  map.getView().getProjection(),
                  map.getView().getResolution(),
                  evt.feature.getGeometry(),
                  activeTool,
                  { timeSeries: scope.timeSeries }
                );

                $q.when(promiseUrl, function(url) {
                  if(url){
                    gnPopup.create({
                      title: activeTool,
                      content: '<div class="gn-popup-iframe ' +
                      activeTool + '">' +
                      '<img style="width:100%;height:100%;" ' +
                      'src="' + url + '" />' +
                      '</div>'
                    });
                  }
                  scope.activeTool = undefined;
                  setTimeout(function() {
                    resetInteraction();
                  }, 300);
                });
              }, this);

            ngeoDecorateInteraction(drawInteraction);
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
          var initFormValues = function() {
            var layer = scope.layer;
            var layerMetadata = layer.get('advancedMetadata');
            scope.params = layer.getSource().getParams() || {};
            scope.datesWithData = {};

            // for ncWMS several extra params are used
            if (scope.isLayerNcwms()) {
              var proj = map.getView().getProjection();
              var bbox = [
                parseFloat(layerMetadata.bbox[0]),
                parseFloat(layerMetadata.bbox[1]),
                parseFloat(layerMetadata.bbox[2]),
                parseFloat(layerMetadata.bbox[3])
              ];

              // use bbox only if it is contained in the world extent
              layer.set('cextent',
                ol.extent.containsExtent(proj.getWorldExtent(), bbox) ?
                  ol.proj.transformExtent(bbox, 'EPSG:4326', proj.getCode()) :
                  proj.getExtent()
              );

              // scale range
              if (layer.get('advancedMetadata').units) {
                scope.colorRange = {
                  step: 1,
                  min: layerMetadata.scaleRange[0],
                  max: layerMetadata.scaleRange[1]
                };

                // oceanotron: range was fetched before
                if (scope.isLayerOceanotron()) {
                  scope.colorRange.min = scope.layer.get('oceanotronScaleRange')[0];
                  scope.colorRange.max = scope.layer.get('oceanotronScaleRange')[1];
                }

                scope.colorscale = {
                  range: [
                    scope.colorRange.min,
                    scope.colorRange.max
                  ]
                }
              }

              scope.timeSeries = {
                from: undefined,
                to: undefined
              };

              // make sure there is a value for LOGSCALE
              if (scope.params.LOGSCALE === undefined) {
                scope.params.LOGSCALE = 'false';
              }
            }

            // styles: init array & set default value
            scope.palettes = gnNcWms.parseStyles(layer);
            scope.palette = {
              value: layerMetadata.defaultPalette || ''
            };

            // elevation (zaxis)
            var elevation = scope.layer.get('elevation');
            if (elevation) {
              scope.elevations = elevation.values;

              // generate bounds for elevation
              if(scope.isLayerOceanotron()) {
                var parts = elevation.values[0].split('/');

                // range description
                scope.elevationMin = parseFloat(parts[0]);
                scope.elevationMax = parseFloat(parts[1]);
                scope.elevRange =
                    scope.elevationMin.toFixed(1) + elevation.units + ' / ' +
                    scope.elevationMax.toFixed(1) + elevation.units

                // initial values
                parts = scope.params.ELEVATION.split('/');
                scope.elevation = {
                  low: parseFloat(parts[0]),
                  high: parseFloat(parts[1])
                };
              }
            }

            // time
            var time = scope.layer.get('time');
            if (time) {
              // initial values
              if (scope.isLayerOceanotron()) {
                parts = scope.params.TIME.split('/');
                scope.ncTime.value = {
                    from: moment(parts[0]).format('DD-MM-YYYY'),
                    to: moment(parts[1]).format('DD-MM-YYYY')
                };
              }
            }

            scope.updateLayerParams();
          };

          /**
           *  Get bounds of color range depending on the current extent.
           *  Called when user wlick on 'auto' button.
           *  Update the slider values to this bounds.
           */
          scope.setAutoColorRanges = function(evt) {
            $(evt.target).addClass('fa-spinner');
            gnNcWms.getColorRangesBounds(scope.layer,
              ol.proj.transformExtent(
                map.getView().calculateExtent(map.getSize()),
                map.getView().getProjection(), 'EPSG:4326').join(','))
            .then(function(response) {
              scope.colorscale.range[0] = response.data.min;
              scope.colorscale.range[1] = response.data.max;
              scope.onColorScaleChange();
              $(evt.target).removeClass('fa-spinner');
            });
          };

          /**
           * Call when the input of the double slider get change.
           * The input is an array of 2 values. It updates the layer
           * with `COLORSCALERANGE` params and refreshes it.
           * @param {?array} v the colorange array
           */
          scope.onColorScaleChange = _.debounce(function() {
            var range = scope.colorscale.range;
            if (angular.isArray(range) && range.length == 2) {
              scope.params.COLORSCALERANGE = range[0] + ',' + range[1];
              scope.updateLayerParams();
            }
          }, 400);

          scope.ncTime = {};

          // watch time argument for NcWMS/Oceanotron layers
          scope.$watch('ncTime.value', function(time) {
            if (!time) {
              return;
            }
            if (time.to && time.from) {
              scope.params.TIME =
                  gnNcWms.getFullTimeValue(scope.layer, time.from) + '/' +
                  gnNcWms.getFullTimeValue(scope.layer, time.to);
            } else if (typeof time === 'string') {
              scope.params.TIME = gnNcWms.getFullTimeValue(scope.layer, time);
            }
            scope.updateLayerParams();
          }, true);

          scope.hasStyles = function() {
            try {
              return Object.keys(scope.palettes).length > 1;
            }
            catch (e) {
              return false;
            }
          };

          scope.updateStyle = function() {
            scope.params.STYLES = scope.palettes[scope.palette.value];
            scope.updateLayerParams();
          };

          // oceanotron only
          scope.updateElevationRange = function() {
            scope.params.ELEVATION = scope.elevation.low + '/' + scope.elevation.high;
            scope.updateLayerParams();
          };

          scope.updateLayerParams = function() {
            scope.layer.getSource().updateParams(scope.params);

            scope.layer.set('legend',
              gnNcWms.updateLegendUrl(scope.layer.get('legend'),
                angular.extend({
                  PALETTE: scope.params.STYLES
                }, scope.params)));
          };

          scope.isLayerNcwms = function() {
            return gnNcWms.isLayerNcwms(scope.layer);
          }
          scope.isLayerOceanotron = function() {
            return gnNcWms.isLayerOceanotron(scope.layer);
          }

          // handle layer change in the directive
          scope.$watch('layer', function(layer) {
            if (!layer || !layer.getSource()) { return }
            scope.loadingMetadata = true;
            gnNcWms.feedOlLayer(layer).then(function() {
              scope.loadingMetadata = false;
              initFormValues();
            })
          });
        }
      };
    }]);
})();
