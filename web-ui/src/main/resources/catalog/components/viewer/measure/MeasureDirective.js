(function() {
  goog.provide('gn_measure_directive');

  var module = angular.module('gn_measure_directive', [
    'ngeo',
    'ui.bootstrap.buttons'
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
  module.service('gnMeasure', [
    function() {

      var mInteraction, updateMeasuresFn, distFeature, areaFeature;
      var options = {
        waitClass: '',
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

      options.drawStyleFunction = (function() {
        var drawStylePolygon = [new ol.style.Style({
          fill: new ol.style.Fill({
            color: [255, 255, 255, 0.4]
          }),
          stroke: new ol.style.Stroke({
            color: [255, 255, 255, 0],
            width: 0
          })
        })];

        return function(feature, resolution) {
          if (feature.getGeometry().getType() === 'Polygon') {
            return drawStylePolygon;
          } else {
            return options.styleFunction(feature, resolution);
          }
        }
      })();

      var initInteraction = function(map) {

        var deregisterFeature;

        var featureOverlay = new ol.FeatureOverlay({
          style: options.drawStyleFunction
        });
        featureOverlay.setMap(map);

        // define the draw interaction used for measure
        mInteraction = new ol.interaction.Draw({
          type: 'Polygon',
          features: featureOverlay.getFeatures(),
          style: options.drawStyleFunction
        });

        Object.defineProperty(mInteraction, 'active', {
          get: function() {
            return map.getInteractions().getArray().indexOf(mInteraction) >= 0;
          },
          set: function(val) {
            if (val) {
              map.addInteraction(mInteraction);
            } else {
              map.removeInteraction(mInteraction);
              featureOverlay.getFeatures().clear();
            }
          }
        });

        mInteraction.on('drawstart',
            function(evt) {
              featureOverlay.getFeatures().clear();

              areaFeature = evt.feature;
              var firstPoint = areaFeature.getGeometry().getCoordinates()[0][0];
              distFeature = new ol.Feature(
                  new ol.geom.LineString([firstPoint]));

              deregisterFeature = areaFeature.on('change',
                  function(evt) {
                    var feature = evt.target;
                    var lineCoords = feature.getGeometry().getCoordinates()[0];

                    distFeature.getGeometry().setCoordinates(lineCoords);
                    updateMeasuresFn();
                  }
                  );
            }, this);

        mInteraction.on('drawend',
            function(evt) {
              var lineCoords = evt.feature.getGeometry().getCoordinates()[0];
              lineCoords.pop();
              distFeature.getGeometry().setCoordinates(lineCoords);

              updateMeasuresFn();
              featureOverlay.addFeature(distFeature);
              areaFeature.unByKey(deregisterFeature);
            }, this);
      };

      this.create = function(map, measureObj, scope) {

        // Update values of measures from features
        updateMeasuresFn = function() {
          scope.$apply(function() {
            measureObj.distance = distFeature.getGeometry().getLength();
            measureObj.surface = areaFeature.getGeometry().getArea();
          });
        };
        initInteraction(map);

        return mInteraction;
      };
    }]);
})();
