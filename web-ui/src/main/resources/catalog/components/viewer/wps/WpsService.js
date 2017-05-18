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
  goog.provide('gn_wps_service');




  goog.require('OWS_1_1_0');
  goog.require('WPS_1_0_0');
  goog.require('XLink_1_0');

  var module = angular.module('gn_wps_service', []);

  // WPS Client
  // Jsonix wrapper to read or write WPS response or request
  var context = new Jsonix.Context(
      [XLink_1_0, OWS_1_1_0, WPS_1_0_0],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wps/1.0.0': 'wps'
        }
      }
      );
  var unmarshaller = context.createUnmarshaller();
  var marshaller = context.createMarshaller();

  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnWpsService
   * @requires $http
   * @requires gnOwsCapabilities
   * @requires gnUrlUtils
   * @requires gnGlobalSettings
   * @requires $q
   *
   * @description
   * The `gnWpsService` service provides methods to call WPS request and
   * manage WPS responses.
   */
  module.service('gnWpsService', [
    '$http',
    'gnOwsCapabilities',
    'gnUrlUtils',
    'gnGlobalSettings',
    'gnMap',
    '$q',
    function($http, gnOwsCapabilities, gnUrlUtils, gnGlobalSettings,
             gnMap, $q) {

      this.WMS_MIMETYPE = 'application/x-ogc-wms';

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnWpsService
       * @name gnWpsService#describeProcess
       *
       * @description
       * Call a WPS describeProcess request and parse the XML response, to
       * returns it as an object.
       *
       * @param {string} uri of the wps service
       * @param {string} processId of the process
       */
      this.describeProcess = function(uri, processId) {
        url = gnOwsCapabilities.mergeDefaultParams(uri, {
          service: 'WPS',
          version: '1.0.0',
          request: 'DescribeProcess',
          identifier: processId
        });

        //send request and decode result
        if (gnUrlUtils.isValid(url)) {
          return $http.get(url, {
            cache: true
          }).then(
              function(response) {
                return unmarshaller.unmarshalString(response.data).value;
              }
          );
        }
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnWpsService
       * @name gnWpsService#execute
       *
       * @description
       * Call a WPS execute request and manage response. The request is called
       * by POST with an OGX XML content built from parameters.
       *
       * @param {string} uri of the wps service
       * @param {string} processId of the process
       * @param {Object} inputs of the process
       * @param {Object} output of the process
       * @param {Object} options such as storeExecuteResponse,
       * lineage and status
       */
      this.execute = function(uri, processId, inputs, responseDocument) {
        var defer = $q.defer();

        var me = this;

        this.describeProcess(uri, processId).then(
            function(data) {
              var description = data.processDescription[0];

              var url = uri;
              var request = {
                name: {
                  localPart: 'Execute',
                  namespaceURI: 'http://www.opengis.net/wps/1.0.0'
                },
                value: {
                  service: 'WPS',
                  version: '1.0.0',
                  identifier: {
                    value: description.identifier.value
                  },
                  dataInputs: {
                    input: []
                  }
                }
              };

              var setInputData = function(input, data) {
                if (input.literalData && data) {
                  request.value.dataInputs.input.push({
                    identifier: {
                      value: input.identifier.value
                    },
                    data: {
                      literalData: {
                        value: data.toString()
                      }
                    }
                  });
                }
                if (input.boundingBoxData) {
                  var bbox = data.split(',');
                  request.value.dataInputs.input.push({
                    identifier: {
                      value: input.identifier.value
                    },
                    data: {
                      boundingBoxData: {
                        dimensions: 2,
                        lowerCorner: [bbox[0], bbox[1]],
                        upperCorner: [bbox[2], bbox[3]]
                      }
                    }
                  });
                }
              };

              for (var i = 0; i < description.dataInputs.input.length; ++i) {
                var input = description.dataInputs.input[i];
                if (inputs[input.identifier.value] !== undefined) {
                  setInputData(input, inputs[input.identifier.value]);
                }
              }

              request.value.responseForm = {
                responseDocument: $.extend(true, {
                  lineage: false,
                  storeExecuteResponse: true,
                  status: false
                }, responseDocument)
              };

              var body = marshaller.marshalString(request);

              $http.post(url, body, {
                headers: {'Content-Type': 'application/xml'}
              }).then(
                  function(data) {
                    var response =
                        unmarshaller.unmarshalString(data.data).value;
                    defer.resolve(response);
                  },
                  function(data) {
                    defer.reject(data);
                  }
              );

            },
            function(data) {
              defer.reject(data);
            }
        );

        return defer.promise;
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnWpsService
       * @name gnWpsService#getStatus
       *
       * @description
       * Get prosess status during execution.
       *
       * @param {string} url of status document
       */
      this.getStatus = function(url) {
        var defer = $q.defer();

        $http.get(url, {
          cache: true
        }).then(
            function(data) {
              var response = unmarshaller.unmarshalString(data.data).value;
              defer.resolve(response);
            },
            function(data) {
              defer.reject(data);
            }
        );

        return defer.promise;
      };

      /**
       * Try to see if the execute response is a reference with a WMS mimetype.
       * If yes, the href is a WMS getCapabilities, we load it and add all
       * the layers on the map.
       * Those new layers has the property `fromWps` to true, to identify them
       * in the layer manager.
       *
       * @param {object} response excecuteProcess response object.
       * @param {ol.Map} map
       * @param {ol.layer.Base} parentLayer
       * @param {object=} opt_options
       */
      this.extractWmsLayerFromResponse =
          function(response, map, parentLayer, opt_options) {

        try {
          var ref = response.processOutputs.output[0].reference;
          if (ref.mimeType == this.WMS_MIMETYPE) {
            gnMap.addWmsAllLayersFromCap(map, ref.href, true).
                then(function(layers) {
                  layers.forEach(function(l) {
                    l.set('fromWps', true);
                    l.set('wpsParent', parentLayer);
                    if (opt_options &&
                        !opt_options.exclude.test(l.get('label'))) {
                      map.addLayer(l);
                    }
                  });
                });
          }
        } catch (e) { // no WMS found }
        }
      };
    }
  ]);

})();
