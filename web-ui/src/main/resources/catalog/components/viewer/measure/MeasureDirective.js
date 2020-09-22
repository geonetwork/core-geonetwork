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
  goog.provide('gn_measure');

  var module = angular.module('gn_measure', [
    'ui.bootstrap.buttons'
  ]);

  var formatLength = function(line, sourceProj) {
    var length = ol.sphere.getLength(line, { projection: sourceProj});
    var output;
    if (length > 1000) {
      output = (Math.round(length / 1000 * 100) / 100) +
        ' ' + 'km';
    } else {
      output = (Math.round(length * 100) / 100) +
        ' ' + 'm';
    }
    return output;
  };

  var formatArea = function(polygon, sourceProj) {
    var area = ol.sphere.getArea(polygon, { projection: sourceProj});
    var output;
    if (area > 10000) {
      output = (Math.round(area / 1000000 * 100) / 100) +
        ' ' + 'km<sup>2</sup>';
    } else {
      output = (Math.round(area * 100) / 100) +
        ' ' + 'm<sup>2</sup>';
    }
    return output;
  };

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMeasure
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

        var featureOverlay = new ol.layer.Vector({
          source: new ol.source.Vector(),
          map: map,
          style: options.drawStyleFunction
        });

        // define the draw interaction used for measure
        mInteraction = new ol.interaction.Draw({
          type: 'Polygon',
          features: featureOverlay.getSource().getFeatures(),
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
              featureOverlay.getSource().clear();
            }
          }
        });

        mInteraction.on('drawstart',
            function(evt) {
              featureOverlay.getSource().clear();

              areaFeature = evt.feature;
              var firstPoint = areaFeature.getGeometry().getCoordinates()[0][0];
              distFeature = new ol.Feature(
                  new ol.geom.LineString([firstPoint]));

              deregisterFeature = areaFeature.on('change',
                  function(evt) {
                    var feature = evt.target;
                    var lineCoords = feature.getGeometry().getCoordinates()[0].slice(0, -1);

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
              featureOverlay.getSource().addFeature(distFeature);
              ol.Observable.unByKey(deregisterFeature);
            }, this);
      };

      this.create = function(map, measureObj, scope) {

        // taken from https://openlayers.org/en/v3.15.0/examples/measure.html
        getGeodesicLength = function(geometry) {
          var sourceProj = map.getView().getProjection();
          return formatLength(geometry, sourceProj);
        };
        getGeodesicArea = function(geometry) {
          var sourceProj = map.getView().getProjection();
          return formatArea(geometry, sourceProj);
        };

        // Update values of measures from features
        updateMeasuresFn = function() {
          scope.$apply(function() {
            measureObj.distance = getGeodesicLength(distFeature.getGeometry());
            measureObj.surface = getGeodesicArea(areaFeature.getGeometry());
          });
        };
        initInteraction(map);

        return mInteraction;
      };
    }]);
})();
