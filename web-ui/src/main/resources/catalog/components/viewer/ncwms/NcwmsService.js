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
    function(gnMap, gnUrlUtils, gnOwsCapabilities, $http) {

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
        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);

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
        var t = [];
        if (angular.isArray(info.supportedStyles)) {
          angular.forEach(info.supportedStyles, function(s) {
            if (s == 'boxfill') {
              if (angular.isArray(info.palettes)) {
                angular.forEach(info.palettes, function(p) {
                  t.push(s + '/' + p);
                });
              }
            }
            else if (s == 'contour') {
              t.push(s + '/');
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
        if (service == 'profile') {
          p.REQUEST = 'GetVerticalProfile';
          p.POINT = gnMap.getTextFromCoordinates(geom);

        } else if (service == 'transect') {
          p.REQUEST = 'GetTransect';
          p.LINESTRING = gnMap.getTextFromCoordinates(geom);
        }
        return gnUrlUtils.append(layer.getSource().getUrls(),
            gnUrlUtils.toKeyValue(p));

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
        return gnUrlUtils.append(layer.getSource().getUrls(),
            gnUrlUtils.toKeyValue(p));
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

        var url = gnUrlUtils.append(layer.getSource().getUrls(),
                  gnUrlUtils.toKeyValue(p));

        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
        return $http.get(proxyUrl);
      };
    }
  ]);
})();
