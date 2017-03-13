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
    function(gnMap, gnUrlUtils, gnOwsCapabilities, $http, gnGlobalSettings) {

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#createNcWmsLayer
       *
       * @description
       * Create sample NCWMS layers.
       *
       * @param {Object} capLayer layer ob from capabilities
       */
      this.createNcWmsLayer = function(capLayer) {
        var source = new ol.source.TileWMS({
          params: {
            LAYERS: 'TEMP'
          },
          url: 'http://tds0.ifremer.fr/thredds/wms/' +
              'CORIOLIS-GLOBAL-CORA04.0-OBS_FULL_TIME_SERIE'
        });
        var layer = new ol.layer.Tile({
          url: 'http://tds0.ifremer.fr/thredds/wms/' +
              'CORIOLIS-GLOBAL-CORA04.0-OBS_FULL_TIME_SERIE',
          type: 'WMS',
          source: source,
          label: 'Super NCWMS'
        });

      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#feedOlLayer
       *
       * @description
       * Call the NCWMS getMetadata request to fill the layers with
       * additionnal info.
       *
       * @param {Object} capLayer layer ob from capabilities
       */
      this.feedOlLayer = function(layer) {
        var url = this.getMetadataUrl(layer);
        var proxyUrl = gnGlobalSettings.proxyUrl + encodeURIComponent(url);

        $http.get(proxyUrl)
            .success(function(json) {
              if (angular.isObject(json)) {
                layer.ncInfo = json;
              }
            });
        return layer;
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#parseTimeSeries
       *
       * @description
       * Parse a time serie from capabilities.
       * Extract from string, 2 limit values.
       * Ex : 2010-12-06T12:00:00.000Z/2010-12-31T12:00:00.000Z
       *
       * @param {string} s serie
       * @return {Object} date
       */
      this.parseTimeSeries = function(s) {
        s = s.trim();
        var as = s.split('/');
        return {
          tsfromD: moment(new Date(as[0])).format('YYYY-MM-DD'),
          tstoD: moment(new Date(as[1])).format('YYYY-MM-DD')
        };
      };
      this.formatTimeSeries = function(from, to) {
        return moment(from, 'DD-MM-YYYY').format(
            'YYYY-MM-DD[T]HH:mm:ss.SSS[Z]') +
            '/' +
            moment(to, 'DD-MM-YYYY').format('YYYY-MM-DD[T]HH:mm:ss.SSS[Z]');
      };

      this.parseStyles = function(info) {
        var t = {};
        if (angular.isArray(info.supportedStyles)) {
          angular.forEach(info.supportedStyles, function(s) {
            if (s == 'boxfill') {
              if (angular.isArray(info.palettes)) {
                angular.forEach(info.palettes, function(p) {
                  t[p] = s + '/' + p;
                });
              }
            }
            else if (s == 'contour') {
              t[s] = s + '/' + p;
            }
          });
        }
        return t;
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnNcWms
       * @name gnNcWms#getDimensionValue
       *
       * @description
       * Read from capabilities object dimension properties.
       * (DEPECRATED)
       *
       * @param {Object} ncInfo capabilities object.
       * @param {string} name type of the dimension.
       * @return {*} dimensions
       */
      this.getDimensionValue = function(ncInfo, name) {
        var value;
        if (angular.isArray(ncInfo.Dimension)) {
          for (var i = 0; i < ncInfo.Dimension.length; i++) {
            if (ncInfo.Dimension[i].name == name) {
              value = ncInfo.Dimension[i].values;
              break;
            }
          }
        }
        else if (angular.isObject(ncInfo.Dimension) &&
            ncInfo.Dimension.name == name) {
          value = ncInfo.Dimension.values[0] ||
              ncInfo.Dimension.values;
        }
        return value;
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
       * @param {aol.geometry} geom param
       * @param {string} service param
       * @return {string} url
       */
      this.getNcwmsServiceUrl = function(layer, proj, geom, service) {
        var p = {
          FORMAT: 'image/png',
          CRS: proj.getCode(),
          LAYER: layer.getSource().getParams().LAYERS
        };
        var time = layer.getSource().getParams().TIME;
        if (time) {
          p.TIME = time;
        }

        if (service == 'profile') {
          p.REQUEST = 'GetVerticalProfile';
          p.POINT = gnMap.getTextFromCoordinates(geom);

        } else if (service == 'transect') {
          p.REQUEST = 'GetTransect';
          p.LINESTRING = gnMap.getTextFromCoordinates(geom);
          var elevation = layer.getSource().getParams().ELEVATION;
          if (elevation) {
            p.ELEVATION = elevation;
          }
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
        var parts = legendUrl.split('?');

        var p = parts.length > 1 ?
            gnUrlUtils.parseKeyValue(parts[1]) : {};
        angular.extend(p, params);

        var sP = gnUrlUtils.toKeyValue(p);
        return gnUrlUtils.append(parts[0], sP);
      };
    }
  ]);
})();
