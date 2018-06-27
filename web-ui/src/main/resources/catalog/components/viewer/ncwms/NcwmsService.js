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

    this.DATE_INPUT_FORMAT = 'DD-MM-YYYY';

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
        if (layer.get('advanced') == true) {
          var url = this.getMetadataUrl(layer);
          var proxyUrl = gnGlobalSettings.proxyUrl + encodeURIComponent(url);
          return $http.get(proxyUrl)
            .success(function(json) {
              if (angular.isObject(json)) {
                layer.ncInfo = json;
                layer.isNcwms = true;
                layer.set('oceanotron', !!layer.ncInfo.multiFeature);
                if(layer.get('oceanotron')) {
                  this.initOceanotronParams(layer);
                }
              }
              else {
                layer.isNcwms = false;
                layer.ncInfo = {
                  'time': {'units': layer.values_.time.units,
                    'values' : layer.values_.time.values},

                  'zaxis': {'units': layer.values_.elevation.units,
                    'values' : layer.values_.elevation.values}
                }
              }
            }.bind(this));
        }
        else {
          return $q.resolve();
        }

      };

      this.initOceanotronParams = function(layer) {
        var ncInfo = layer.ncInfo;
        var elevation = '0/1';
        var palettes = this.parseStyles(ncInfo);
        var styles = palettes[ncInfo.defaultPalette || ncInfo.palettes[0]];

        var day = new Date();
        day.setDate(day.getDate());
        var to = moment(day).format(this.DATE_INPUT_FORMAT);
        day.setDate(day.getDate() - 2);
        var from = moment(day).format(this.DATE_INPUT_FORMAT);

        layer.getSource().updateParams({
          STYLES: styles,
          ELEVATION: elevation,
          TIME: this.formatTimeSeries(from, to)
        })

      };

      this.parseStyles = function(info) {
        var t = {};
        if (angular.isArray(info.supportedStyles) &&
          info.supportedStyles.length) {
          angular.forEach(info.supportedStyles, function(s) {
            if (s == 'boxfill') {
              if (angular.isArray(info.palettes)) {
                angular.forEach(info.palettes, function(p) {
                  t[p] = s + '/' + p;
                });
              }
            }
            else if (s == 'contour') {
              t[s] = s; // TODO ????? + '/' + p;
            }
          });
        }
        else {
          info.palettes.forEach(function(p) {
            t[p] = p;
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
      this.getNcwmsServiceUrl = function(layer, proj, geom, service, options) {
        var p = angular.extend({
          FORMAT: 'image/png',
          CRS: proj.getCode(),
          LAYER: layer.getSource().getParams().LAYERS
        }, options);

        var time = layer.getSource().getParams().TIME;
        if (time) {
          p.TIME = time;
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
    }
  ]);
})();
