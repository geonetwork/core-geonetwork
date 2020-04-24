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
  goog.provide('gn_geometry_service');

  var module = angular.module('gn_geometry_service', []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_geometry.service:gnGeometryService
   * @requires gnMap
   *
   * @description
   * The `gnGeometryService` service provides utility related to handling
   * geometry and mime types
   */
  module.service('gnGeometryService', [
    function() {

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#getFormatFromMimeType
       *
       * @description
       * Guess the output format to use in a gn-geometry-tool directive based
       * on a mime type received from a DescribeProcess call.
       *
       * @param {string} mimeType mime type from the process description
       * @return {string} format to be used by a gn-geometry-tool directive
       */
      this.getFormatFromMimeType = function(mimeType) {
        var parts = mimeType.split(';');
        parts.forEach(function(p) {
          p = p.trim().toLowerCase();
        });

        switch (parts[0]) {
          case 'application/json':
            return 'geojson';

          case 'application/wkt':
            return 'wkt';

          case 'application/gml+xml':
          case 'text/xml':
            return 'gml';

          default:
            return 'object';
        }
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#getCommonLayer
       *
       * @description
       * This fetches a OL vector layer that is common to all Geometry Tools
       * If non existent, the layer will be created
       *
       * @param {ol.Map} map open layers map
       * @return {ol.layer.Vector} vector layer
       */
      this.getCommonLayer = function(map) {
        var commonLayer = null;
        map.getLayers().getArray().forEach(function(layer) {
          if (layer.get('name') === 'geometry-tool-layer') {
            commonLayer = layer;
          }
        });

        if (commonLayer) {
          return commonLayer;
        }

        // layer & source
        var source = new ol.source.Vector({
          useSpatialIndex: true,
          features: new ol.Collection()
        });
        commonLayer = new ol.layer.Vector({
          source: source,
          name: 'geometry-tool-layer',
          style: [
            new ol.style.Style({  // this is the default editing style
              fill: new ol.style.Fill({
                color: 'rgba(255, 255, 255, 0.5)'
              }),
              stroke: new ol.style.Stroke({
                color: 'white',
                width: 5
              })
            }),
            new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'rgba(0, 153, 255, 1)',
                width: 3
              }),
              image: new ol.style.Circle({
                radius: 6,
                fill: new ol.style.Fill({
                  color: 'rgba(0, 153, 255, 1)'
                }),
                stroke: new ol.style.Stroke({
                  color: 'white',
                  width: 1.5
                })
              })
            })
          ]
        });
        commonLayer.setZIndex(100);

        // add our layer to the map
        map.addLayer(commonLayer);

        return commonLayer;
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#printGeometryOutput
       *
       * @description
       * This prints the output of a geometry tool according to options given
       *
       * @param {ol.Map} map open layers map
       * @param {ol.Feature} feature feature to output
       * @param {object} options options
       * @param {string} options.crs default is EPSG:4326
       * @param {string} options.format default is GML
       * @param {bool} options.outputAsWFSFeaturesCollection default is false
       * @param {bool} options.outputAsFeatures default is false
       * @param {string} options.gmlFeatureElement feature element name
       * @param {string} options.gmlFeatureNS feature element namespace
       * @return {string | ol.geom.Geometry | Array.<ol.Feature>} output
       *  as string or object
       */
      this.printGeometryOutput = function(map, feature, options) {
        var options = angular.extend({
          crs: 'EPSG:4326',
          format: 'gml'
        }, options);

        // clone & transform geom
        var outputGeom = feature.getGeometry().clone().transform(
            map.getView().getProjection(),
            options.crs || 'EPSG:4326'
            );
        var outputFeature = new ol.Feature({
          geometry: outputGeom
        });

        // set id on feature
        feature.setId('geometry-tool-output');

        var formatLabel = options.format.toLowerCase();
        var format;
        var outputValue;
        switch (formatLabel) {
          case 'json':
          case 'geojson':
            format = new ol.format.GeoJSON();
            if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              outputValue = format.writeGeometry(outputGeom);
            }
            break;

          case 'wkt':
            format = new ol.format.WKT();
            if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              outputValue = format.writeGeometry(outputGeom);
            }
            break;

          case 'gml':
            format = new ol.format.GML({
              featureNS: options.gmlFeatureNS ||
                  'http://mapserver.gis.umn.edu/mapserver',
              featureType: options.gmlFeatureElement || 'features',
              srsName: options.crs
            });

            if (options.outputAsWFSFeaturesCollection) {
              outputValue =
                  '<wfs:FeatureCollection ' +
                  'xmlns:wfs="http://www.opengis.net/wfs">' +
                  format.writeFeatures([outputFeature]) +
                  '</wfs:FeatureCollection>';
            } else if (options.outputAsFeatures) {
              outputValue = format.writeFeatures([outputFeature]);
            } else {
              var nodes = format.writeFeaturesNode([outputFeature])
                  .firstChild.childNodes;
              var geom = null;
              for (var i = 0; i < nodes.length; i++) {
                var node = nodes.item(0);
                if (node.localName === outputFeature.getGeometryName()) {
                  geom = node;
                }
              }
              if (!geom) {
                console.warn('No geometry found for feature', feature);
                return null;
              }
              outputValue = geom.innerHTML;
            }
            break;

          // no valid format specified: output as object + give warning
          default:
            console.warn('No valid output format specified for ' +
                'gn-geometry-tool (value=' + options.format + '); ' +
                'outputting geometry as object');

          case 'object':
            if (options.outputAsFeatures) {
              outputValue = [outputFeature];
            } else {
              outputValue = outputGeom;
            }
            break;
        }

        return outputValue;
      };

      /**
       * @ngdoc method
       * @methodOf gn_geometry.service:gnGeometryService
       * @name gnGeometryService#printGeometryOutput
       *
       * @description
       * This parses a text to create an Open Layers geometry object.
       *
       * @param {ol.Map} map open layers map
       * @param {string} input as text
       * @param {object} options options
       * @param {string} options.crs default is EPSG:4326
       * @param {string} options.format default is GML
       * @param {string} options.gmlFeatureElement feature element name
       * @param {string} options.gmlFeatureNS feature element ns
       * @return {ol.geom.Geometry} geometry object
       */
      this.parseGeometryInput = function(map, input, options) {
        var options = angular.extend({
          crs: 'EPSG:4326',
          format: 'gml'
        }, options);

        var formatLabel = options.format.toLowerCase();
        var format;
        var outputProjection = map.getView().getProjection();
        var outputValue = null;
        switch (formatLabel) {
          case 'json':
          case 'geojson':
            format = new ol.format.GeoJSON();
            outputValue = format.readGeometry(input, {
              dataProjection: options.crs,
              featureProjection: outputProjection
            });
            break;

          case 'wkt':
            format = new ol.format.WKT();
            outputValue = format.readGeometry(input, {
              dataProjection: options.crs,
              featureProjection: outputProjection
            });
            break;

          case 'gml':
            format = new ol.format.GML({
              featureNS: 'http://www.opengis.net/gml',
              featureType: 'feature'
            });
            var fullXml = '<featureMembers>' +
                '<gml:feature xmlns:gml="http://www.opengis.net/gml">' +
                '<geometry>' +
                input + '</geometry></gml:feature></featureMembers>';

            var feature = format.readFeatures(
              fullXml.replace(
                /http:\/\/www.opengis.net\/gml\/3.2/g,
                'http://www.opengis.net/gml')
                .replace(/urn:x-ogc:def:crs:EPSG:6.6:4326/g,
                  'urn:ogc:def:crs:EPSG:6.6:4326'), {
              dataProjection: options.crs,
              featureProjection: outputProjection
            })[0];
            outputValue = feature.getGeometry();
            break;

          // no valid format specified: handle as object
          default:
          case 'object':
            if (!input instanceof ol.geom.Geometry) {
              console.error('gn-geometry-tool input was supposed to be a ' +
                  'ol.geom.Geometry object but was something else, ' +
                  'skipping parse.', input);
              return outputValue;
            }
            outputValue = input.clone.transform(options.crs, outputProjection);
            break;
        }

        return outputValue;
      };
    }
  ]);
})();
