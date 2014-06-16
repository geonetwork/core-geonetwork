(function() {
  goog.provide('gn_measure_directive');

  var module = angular.module('gn_measure_directive', [
  ]);

  module.filter('measure', function() {
    return function(floatInMeter, type, units) {
      // Type could be: volume, area or distance
      var factor = 1000;
      switch (type) {
        case 'volume': factor = Math.pow(factor, 3);
        break;
        case 'area': factor = Math.pow(factor, 2);
        break;
        default: break;
      }
      units = units || [' km', ' m'];
      floatInMeter = floatInMeter || 0;
      var measure = floatInMeter.toFixed(2);
      var km = Math.floor(measure / factor);

      if (km <= 0) {
        if (parseInt(measure) == 0) {
          measure = 0;
        }
        return measure + units[1];
      }

      var str = '' + km;
      var m = Math.floor(Math.floor(measure) % factor * 100 / factor);

      if (m > 0) {
        str += '.';
        if (m < 10) {
          str += '0';
        }
        str += m;
      }
      str += ' ' + units[0];
      return str;
    };
  });

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnMeasure', [
    '$document',
    '$rootScope',
    'gnDefinePropertiesForLayer',
    'gaLayerFilters',
    function($document, $rootScope, gnDefinePropertiesForLayer, gaLayerFilters) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/measure/' +
            'partials/measure.html',
        scope: {
          map: '=gnMeasureMap',
          isActive: '=gnMeasureActive'
        },
        link: function(scope, element, attrs) {
          var bodyEl = angular.element($document[0].body);
          var sketchFeatArea, sketchFeatDistance, sketchFeatAzimuth;
          var deregister, deregisterFeature;

          scope.options = {
            waitClass: 'ga-measure-wait',
            isProfileActive: false,
            profileOptions: {
                              xLabel: 'profile_x_label',
                              yLabel: 'profile_y_label',
                              margin: {
                                top: 20,
                                right: 20,
                                bottom: 40,
                                left: 60
                              },
                              width: 600,
                              height: 350,
                              elevationModel: 'COMB'
            },
            styleFunction: (function() {
              var styles = {};

              var stroke = new ol.style.Stroke({
                color: [255, 0, 0, 1],
                width: 3
              });

              var strokeDashed = new ol.style.Stroke({
                color: [255, 0, 0, 1],
                width: 3,
                lineDash: [8]
              });
              var fill = new ol.style.Fill({
                color: [255, 0, 0, 0.4]
              });

              styles['Polygon'] = [
                new ol.style.Style({
                  fill: fill,
                  stroke: strokeDashed
                })
              ];

              styles['LineString'] = [
                new ol.style.Style({
                  stroke: strokeDashed
                })
              ];

              styles['Point'] = [
                new ol.style.Style({
                  image: new ol.style.Circle({
                    radius: 4,
                    fill: fill,
                    stroke: stroke
                  })
                })
              ];

              styles['Circle'] = [
                new ol.style.Style({
                  stroke: stroke
                })
              ];

              return function(feature, resolution) {
                                return styles[feature.getGeometry().getType()];
              };
            })()
          };

          var styleFunction = scope.options.styleFunction;

          var layer = new ol.layer.Vector({
            source: new ol.source.Vector(),
            style: scope.options.styleFunction
          });
          gnDefinePropertiesForLayer(layer);
          layer.displayInLayerManager = false;
          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilter = gaLayerFilters.selected;

          // Creates the additional overlay to display azimuth circle
          var featuresOverlay = new ol.FeatureOverlay({
            style: scope.options.styleFunction
          });

          var drawArea = new ol.interaction.Draw({
            type: 'Polygon',
            minPointsPerRing: 2,
            style: scope.options.drawStyleFunction
          });

          // Activate the component: add listeners, last features drawn and draw
          // interaction.
          var activate = function() {
            var isFinishOnFirstPoint;
            scope.map.addLayer(layer);
            scope.map.addInteraction(drawArea);
            featuresOverlay.setMap(scope.map);

            // Add events
            deregister = [
              // Move measure layer  on each changes in the list of layers
              // in the layer manager.
              scope.$watchCollection('layers | filter:layerFilter',
                  moveLayerOnTop),

              drawArea.on('drawstart', function(evt) {
                var nbPoint = 1;
                var isSnapOnLastPoint = false;

                // Clear the layer
                layer.getSource().clear();

                // Initialisation of the sketchFeatures
                sketchFeatArea = evt.feature;
                var firstPoint = sketchFeatArea.getGeometry()
                          .getCoordinates()[0][0];
                sketchFeatDistance = new ol.Feature(
                    new ol.geom.LineString([firstPoint]));
                sketchFeatAzimuth = new ol.Feature(
                    new ol.geom.Circle(firstPoint, 0));
                featuresOverlay.addFeature(sketchFeatAzimuth);

                // Update the profile
                if (scope.options.isProfileActive) {
                  updateProfileDebounced();
                }

                deregisterFeature = sketchFeatArea.on('change',
                    function(evt) {
                      var feature = evt.target; //sketchFeatArea
                      var lineCoords = feature.getGeometry()
                              .getCoordinates()[0];

                      if (nbPoint != lineCoords.length) {
                        // A point is added
                        nbPoint++;

                        // Update the profile
                        if (scope.options.isProfileActive) {
                          updateProfileDebounced();
                        }

                      } else {
                        // We update features and measures
                        var lastPoint = lineCoords[lineCoords.length - 1];
                        var lastPoint2 = lineCoords[lineCoords.length - 2];

                        var isSnapOnFirstPoint = (lastPoint[0] == firstPoint[0] &&
                                lastPoint[1] == firstPoint[1]);

                        // When the last change event is triggered the polygon is
                        // closed so isSnapOnFirstPoint is true. We need to know
                        // if on the change event just before, the snap on last
                        // point was active.
                        isFinishOnFirstPoint = (!isSnapOnLastPoint &&
                                isSnapOnFirstPoint);

                        isSnapOnLastPoint = (lastPoint[0] == lastPoint2[0] &&
                                lastPoint[1] == lastPoint2[1]);

                        if (isSnapOnLastPoint) {
                          // In that case the 2 last points of the coordinates
                          // array are identical, so we remove the useless one.
                          lineCoords.pop();
                        }
                        sketchFeatDistance.getGeometry()
                                .setCoordinates(lineCoords);

                        updateMeasures();

                        if (!isSnapOnFirstPoint) {
                          if (lineCoords.length == 2) {
                            sketchFeatAzimuth.getGeometry()
                                    .setRadius(scope.distance);
                          } else if (!isSnapOnLastPoint) {
                            sketchFeatAzimuth.getGeometry().setRadius(0);
                          }
                        }
                      }
                    }
                    );
              }),

              drawArea.on('drawend', function(evt) {

                if (!isFinishOnFirstPoint) {
                  // The sketchFeatureArea is automatically closed by the draw
                  // interaction even if the user has finished drawing on the
                  // last point. So we remove the useless coordinates.
                  var lineCoords = sketchFeatDistance.getGeometry()
                            .getCoordinates();
                  lineCoords.pop();
                  sketchFeatDistance.getGeometry().setCoordinates(lineCoords);
                }

                // Update the layer
                updateLayer(isFinishOnFirstPoint);

                // Update measures
                updateMeasures();

                // Clear the additional overlay
                featuresOverlay.getFeatures().clear();

                // Unregister the change event
                sketchFeatArea.unByKey(deregisterFeature);

                // Update the profile
                if (scope.options.isProfileActive) {
                  bodyEl.addClass(scope.options.waitClass);
                  updateProfileDebounced();
                }

              })
            ];
          };


          // Deactivate the component: remove listeners, features and draw
          // interaction.
          var deactivate = function() {
            featuresOverlay.getFeatures().clear();
            featuresOverlay.setMap(null);
            scope.map.removeInteraction(drawArea);
            scope.map.removeLayer(layer);

            // Remove events
            if (deregister) {
              for (var i = deregister.length - 1; i >= 0; i--) {
                var elt = deregister[i];
                if (elt instanceof Function) {
                  elt();
                } else {
                  elt.src.unByKey(elt);
                }
              }
            }
            bodyEl.removeClass(scope.options.waitClass);
          };


          // Add sketch features to the layer
          var updateLayer = function(isFinishOnFirstPoint) {
            if (sketchFeatArea) {
              var lineCoords = sketchFeatDistance.getGeometry()
                        .getCoordinates();

              if (lineCoords.length == 2) {
                layer.getSource().addFeature(sketchFeatAzimuth);
              }
              layer.getSource().addFeature(isFinishOnFirstPoint ?
                  sketchFeatArea : sketchFeatDistance);
            }
          };

          // Update value of measures from the sketch features
          var updateMeasures = function() {
            scope.$apply(function() {
              var coords = sketchFeatDistance.getGeometry().getCoordinates();
              scope.distance = sketchFeatDistance.getGeometry().getLength();
              scope.azimuth = calculateAzimuth(coords[0], coords[1]);
              scope.surface = sketchFeatArea.getGeometry().getArea();
            });
          };

          // Calulate the azimuth from 2 points
          var calculateAzimuth = function(pt1, pt2) {
            if (!pt1 || !pt2) {
              return undefined;
            }

            var x = pt2[0] - pt1[0];
            var y = pt2[1] - pt1[1];
            var rad = Math.acos(y / Math.sqrt(x * x + y * y));
            var factor = x > 0 ? 1 : -1;
            return (360 + (factor * rad * 180 / Math.PI)) % 360;
          };

          // Update profile functions
          var updateProfile = function() {
            if (scope.options.isProfileActive &&
                sketchFeatDistance &&
                sketchFeatDistance.getGeometry() &&
                sketchFeatDistance.getGeometry()
                           .getCoordinates().length >= 1) {
              scope.options.drawProfile(sketchFeatDistance);
            } else {
              bodyEl.removeClass(scope.options.waitClass);
            }
          };
          var updateProfileDebounced = function() {};
          /*  } gaDebounce.debounce(updateProfile, 500,
                    false);*/


          // Watchers
          scope.$watch('isActive', function(active) {
            $rootScope.isMeasureActive = active;
            if (active) {
              activate();
            } else {
              deactivate();
            }
          });
          scope.$watch('options.isProfileActive', function(active) {
            if (active) {
              bodyEl.addClass(scope.options.waitClass);
              updateProfileDebounced();
            } else {
              bodyEl.removeClass(scope.options.waitClass);
            }
          });

          // Move the draw layer on top
          var moveLayerOnTop = function() {
            var idx = scope.layers.indexOf(layer);
            if (idx != -1 && idx !== scope.layers.length - 1) {
              scope.map.removeLayer(layer);
              scope.map.addLayer(layer);
            }
          };

          // Listen Profile directive events
          var sketchFeatPoint = new ol.Feature(new ol.geom.Point([0, 0]));
          $rootScope.$on('gaProfileMapPositionActivate',
              function(event, coords) {
                featuresOverlay.addFeature(sketchFeatPoint);
              });
          $rootScope.$on('gaProfileMapPositionUpdated',
              function(event, coords) {
                sketchFeatPoint.getGeometry().setCoordinates(coords);
              });
          $rootScope.$on('gaProfileMapPositionDeactivate', function(event) {
            featuresOverlay.removeFeature(sketchFeatPoint);
          });
          $rootScope.$on('gaProfileDataLoaded', function(ev, data) {
            bodyEl.removeClass(scope.options.waitClass);
          });
          $rootScope.$on('gaProfileDataUpdated', function(ev, data) {
            bodyEl.removeClass(scope.options.waitClass);
          });

        }
      };
    }]);

})();
