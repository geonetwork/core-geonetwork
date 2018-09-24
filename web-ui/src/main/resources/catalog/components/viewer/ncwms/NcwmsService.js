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
  goog.provide('gn_ncwms_service');

  var module = angular.module('gn_ncwms_service', []);

  var OCEANOTRON_INFO_URL_TEMPLATE =
    '**path**/OPENDAP/opendap/**layer**.dds';

  var LAYERTYPE_WMS = 0;
  var LAYERTYPE_WMS_NCWMS = 1;
  var LAYERTYPE_WMS_OCEANOTRON = 2;

  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnNcWms
   * @requires gnMap
   * @requires gnOwsCapabilities
   * @requires gnUrlUtils
   * @requires $http
   *
   * @description
   * The `gnNcWms` service provides provides some method to help
   * managing NCWMS layers.
   */
  module.service('gnNcWms', [
    'gnMap',
    'gnUrlUtils',
    'gnOwsCapabilities',
    '$http',
    'gnGlobalSettings',
    '$q',
    function(gnMap, gnUrlUtils, gnOwsCapabilities, $http, gnGlobalSettings,
             $q) {

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#feedOlLayer
       *
       * @description
       * This will attempt requests on the WMS service to determine the type
       * of the current layer (LAYERTYPE_*)
       *
       * @param {ol.layer} layer param
       * @returns {defer} promise
       */
      this.feedOlLayer = function(layer) {
        // advanced means the layer has advanced functionalities (time, style...)
        if (!layer.get('advanced')) {
          return $q.resolve();
        }

        // already done
        if (layer.get('advancedMetadata')) {
          return $q.resolve();
        }

        var url = this.getMetadataUrl(layer);
        return $http.get(url)
          .then(function(response) {
            var json = response.data;
            // metadata object was received: layer is a ncwms/oceanotron
            if (angular.isObject(json)) {
              layer.set('advancedMetadata', json);
              if (json.multiFeature || json.queryable) {
                layer.set('advancedType', LAYERTYPE_WMS_OCEANOTRON);
              } else {
                layer.set('advancedType', LAYERTYPE_WMS_NCWMS);
              }

              this.formatNcwmsAvailableDates(layer);

              if (this.isLayerOceanotron(layer)) {
                return this.initOceanotronParams(layer);
              }
            }
            // build the metadata object ourselves to handle wms params
            else {
              layer.set('advancedType', LAYERTYPE_WMS);
              layer.set('advancedMetadata', {
                simpleWMS: true
              })
            }
          }.bind(this));
      };

      // dates are reformatted from iso8601 string to epoch int
      this.formatNcwmsAvailableDates = function(layer) {
        if (!layer.get('time')) { return; }
        if (this.isLayerOceanotron(layer)) {
          var dates = [];
          var dateParts = layer.get('time').values[0].split('/');
          var current = moment(dateParts[0], 'YYYY-MM-DD');
          var end = moment(dateParts[1], 'YYYY-MM-DD');
          while (current.isBefore(end)) {
            dates.push(current.valueOf());
            current.add(1, 'day');
          }
          layer.get('time').values = dates;
        } else {
          layer.get('time').values = layer.get('time').values.map(function(date) {
            return moment(date).valueOf();
          });
        }
      }

      this.initOceanotronParams = function(layer) {
        var metadata = layer.get('advancedMetadata');
        var palettes = this.parseStyles(metadata);

        var times = layer.get('time').values;
        var from = moment(times[0]).toISOString();
        var to = moment(times[times.length - 1]).toISOString();

        var elevParts = layer.get('elevation').values[0].split('/');

        layer.getSource().updateParams({
          STYLES: palettes[metadata.defaultPalette || metadata.palettes[0]],
          ELEVATION: elevParts[0] + '/' + elevParts[1],
          TIME: from + '/' + to,
          TIMEUNIT: layer.get('time').units
        })

        var promise1 = this.getOceanotronInfo(layer).then(function(type) {
          layer.set('oceanotronType', type);
        });
        var promise2 = this.getColorRangesBounds(layer, [-180, -90, 180, 90])
        .then(function(response) {
          layer.set('oceanotronScaleRange', [response.data.min, response.data.max]);
        });

        return $q.all([promise1, promise2]);
      };

      this.parseStyles = function(layerMetadata) {
        var t = {};
        if (angular.isArray(layerMetadata.supportedStyles) &&
          layerMetadata.supportedStyles.length) {
          angular.forEach(layerMetadata.supportedStyles, function(s) {
            if (s === 'contour') {
              t[s] = s; // TODO ????? + '/' + p;
            } else if (angular.isArray(layerMetadata.palettes)) {
              angular.forEach(layerMetadata.palettes, function(p) {
                  t[p] = s + '/' + p;
              });
            }
          });
        }
        else {
          layerMetadata.palettes.forEach(function(p) {
            t[p] = p;
          });
        }
        return t;
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#getNcwmsServiceUrl
       *
       * @description
       * Compute ncWMS specific services url from parameters.
       * Could be `GetVerticalProfile` `GetTransect`.
       *
       * @param {ol.layer} layer to request
       * @param {string} proj param
       * @param {ol.geometry} geom param
       * @param {string} service param
       * @return {string} url
       */
      this.getNcwmsServiceUrl = function(layer, proj, geom, service, options) {
        var p = angular.extend({
          FORMAT: 'image/png',
          CRS: proj.getCode(),
          LAYER: layer.getSource().getParams().LAYERS
        }, options);

        var time = layer.getSource().getParams().TIME;
        if (time && !p.TIME) {
          p.TIME = time;
        }
        var elevation = layer.getSource().getParams().ELEVATION;
        if (elevation) {
          p.ELEVATION = elevation;
        }

        if (service == 'profile') {
          p.REQUEST = 'GetVerticalProfile';
          p.POINT = gnMap.getTextFromCoordinates(geom);
        } else if (service == 'time') {
          p.REQUEST = 'GetTimeseries';
          p.POINT = gnMap.getTextFromCoordinates(geom);
        } else if (service == 'transect') {
          p.REQUEST = 'GetTransect';
          p.LINESTRING = gnMap.getTextFromCoordinates(geom);
        }
        return gnUrlUtils.append(layer.get('url'), gnUrlUtils.toKeyValue(p));
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#getMetadataUrl
       *
       * @description
       * Get metadataurl with item=layerDetails to retrieve
       * all layer basic informations
       * @param {ol.layer} layer param
       * @return {string} url
       */
      this.getMetadataUrl = function(layer) {
        var p = {
          request: 'GetMetadata',
          item: 'layerDetails',
          layerName: layer.getSource().getParams().LAYERS
        };
        return gnUrlUtils.append(layer.get('url'), gnUrlUtils.toKeyValue(p));
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#getColorRangesBounds
       *
       * @description
       * Get auto colorange bounds depending on an extent.
       * @param {ol.layer} layer param
       * @param {Array} extent for range
       * @return {*} promise
       */
      this.getColorRangesBounds = function(layer, extent) {
        var p = {
          request: 'GetMetadata',
          item: 'minmax',
          width: 50,
          height: 50,
          srs: 'EPSG:4326',
          layers: layer.getSource().getParams().LAYERS,
          bbox: extent
        };
        var time = layer.getSource().getParams().TIME;
        if (time) {
          p.TIME = time;
        }
        var elevation = layer.getSource().getParams().ELEVATION;
        if (elevation) {
          p.ELEVATION = elevation;
        }

        var url = gnUrlUtils.append(layer.get('url'), gnUrlUtils.toKeyValue(p));

        return $http.get(gnGlobalSettings.proxyUrl + encodeURIComponent(url));
      };

      /**
       * Update the `legend` property of the layer depending on
       * layers parameters.
       *
       * @param {string} legendUrl
       * @return {string}
       */
      this.updateLengendUrl = function(legendUrl, params) {
        if(!legendUrl) return;

        var parts = legendUrl.split('?');

        var p = parts.length > 1 ?
            gnUrlUtils.parseKeyValue(parts[1]) : {};
        angular.extend(p, params);

        var sP = gnUrlUtils.toKeyValue(p);
        return gnUrlUtils.append(parts[0], sP);
      };


      /**
       * Exemple
       * http://www.ifremer.fr/oceanotron/OPENDAP/opendap/INS-CORIOLIS-GLO-NRT-OBS_TRAJECTORIES_LATEST.dds
       * @param {string} layerName
       * @returns {string}
       */
      this.getOceanotronInfoUrl = function(layer) {
        var name = layer.getSource().getParams().LAYERS.split('/')[0];
        var u = gnUrlUtils.urlResolve(layer.get('url'));
        var path = u.protocol + '://' + u.host + '/' +
          u.pathname.split('/')[1];
        return OCEANOTRON_INFO_URL_TEMPLATE.replace('**layer**', name)
          .replace('**path**', path);
      };

      this.getOceanotronInfo = function(layer) {
        return $http.get(this.getOceanotronInfoUrl(layer)
        ).then(function(response) {
          var type;
          if(/}\s*trajectory|time_series\s*;/.test(response.data)) {
            type = 'time';
          }
          else if(/}\s*profile\s*;/.test(response.data)) {
            type = 'profile';
          }
          return type;
        });
      };

      this.parseOceanotronXmlCapabilities = function(text) {
        var doc = ol.xml.parse(text);
        var fIds = doc.getElementsByTagName('id');
        var ids = [].map.call(fIds, function(node) {
          return ol.xml.getAllTextContent(node);
        }).join(',');

        return ids;
      }

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#getResultImageUrl
       *
       * @description
       * Will return the image url of the result once the process is over
       *
       * @param {ol.layer} layer to request
       * @param {string} proj param
       * @param {number} resolution param
       * @param {ol.geometry} geom param (point or line depending on service)
       * @param {string} service param
       * @param {Object} options can hold timeSeries property
       * @return {defer} promise, will resolve with the url to display
       */
      this.getResultImageUrl = function(layer, proj, resolution, geom, service, options) {
        var layerParams = { INFO_FORMAT: 'image/png' };
        var gfiUrl;
        var me = this;

        switch(layer.get('advancedType')) {
          // simple NCWMS call
          case LAYERTYPE_WMS_NCWMS:
          if (service === 'time' && options.timeSeries) {
            layerParams.TIME =
                this.getFullTimeValue(layer, options.timeSeries.from) + '/' +
                this.getFullTimeValue(layer, options.timeSeries.to)
          }
          return this.getNcwmsServiceUrl(
            layer,
            proj,
            geom.getCoordinates(),
            service,
            layerParams
          );

          // first a GFI, then NCWMS call
          case LAYERTYPE_WMS_OCEANOTRON:
          layerParams.INFO_FORMAT = 'text/xml';
          gfiUrl = layer.getSource().getGetFeatureInfoUrl(
            geom.getCoordinates(), resolution,proj, layerParams
          );
          return $http.get(gfiUrl).then(
            function(response) {
              var ids =
                  me.parseOceanotronXmlCapabilities(response.data);

              if(!ids) return null;
              return me.getNcwmsServiceUrl(
                layer,
                proj,
                geom.getCoordinates(),
                service,
                { LAYER: ids }
              );
            });
        }

        console.warn('Cannot request a service URL on a non-Ncwms layer');
        return $q.resolve();
      }

      this.isLayerNcwms = function(layer) {
        return layer.get('advancedType') === LAYERTYPE_WMS_NCWMS ||
        layer.get('advancedType') === LAYERTYPE_WMS_OCEANOTRON;
      }

      this.isLayerOceanotron = function(layer) {
        return layer.get('advancedType') === LAYERTYPE_WMS_OCEANOTRON;
      }

      // returns an iso8601 formatted string
      this.getFullTimeValue = function(layer, inputDate) {
        var times = layer.get('time').values;
        var inputMoment = moment(inputDate, 'DD-MM-YYYY');
        var diff;
        for (var i = 0; i < times.length; i++) {
          diff = inputMoment.diff(times[i], 'days', true)

          if ((i === 0 && diff < 0) ||
              (i === times.length - 1 && diff > 0) ||
              Math.abs(diff) < 1) {
            return moment(times[i]).toISOString();
          }
        }
        console.warn('Full time value not found for input date ' + inputDate);
        return null;
      }
    }
  ]);
})();
