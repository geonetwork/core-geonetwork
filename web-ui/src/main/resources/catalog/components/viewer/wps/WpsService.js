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










  goog.require('GML_3_1_1');
  goog.require('OWS_1_1_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WPS_1_0_0');
  goog.require('XLink_1_0');

  var module = angular.module('gn_wps_service', []);

  // WPS Client
  // Jsonix wrapper to read or write WPS response or request
  var context = new Jsonix.Context(
      [XLink_1_0, OWS_1_1_0, WPS_1_0_0, GML_3_1_1, SMIL_2_0, SMIL_2_0_Language],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wps/1.0.0': 'wps',
          'http://www.opengis.net/gml': 'gml'
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
    '$translate',
    function($http, gnOwsCapabilities, gnUrlUtils, gnGlobalSettings,
             gnMap, $q, $translate) {

      this.WMS_MIMETYPE_REGEX = /.*ogc-wms/;

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
       * @param {Object} options object
       * @param {boolean} options.cancelPrevious if true, previous ongoing
       *  requests are cancelled
       */
      this.describeProcess = function(uri, processId, options) {
        url = gnOwsCapabilities.mergeDefaultParams(uri, {
          service: 'WPS',
          version: '1.0.0',
          request: 'DescribeProcess',
          identifier: processId
        });
        options = options || {};

        // cancel ongoing request
        if (options.cancelPrevious && this.descProcCanceller) {
          this.descProcCanceller.resolve();
        }

        // create a promise (will be used to cancel request)
        this.descProcCanceller = $q.defer();

        //send request and decode result
        if (gnUrlUtils.isValid(url)) {
          return $http.get(url, {
            cache: true,
            timeout: this.descProcCanceller.promise
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
       * @name gnWpsService#getCapabilities
       *
       * @description
       * Get a list of processes available on the URL through a GetCap call.
       *
       * @param {string} url of the wps service
       * @param {Object} options object
       * @param {boolean} options.cancelPrevious if true, previous ongoing
       *  requests are cancelled
       */
      this.getCapabilities = function(url, options) {
        url = gnOwsCapabilities.mergeDefaultParams(url, {
          service: 'WPS',
          version: '1.0.0',
          request: 'GetCapabilities'
        });
        options = options || {};

        // cancel ongoing request
        if (options.cancelPrevious && this.getCapCanceller) {
          this.getCapCanceller.resolve();
        }

        // create a promise (will be used to cancel request)
        this.getCapCanceller = $q.defer();

        // send request and decode result
        return $http.get(url, {
          cache: true,
          timeout: this.getCapCanceller.promise
        }).then(function(response) {
          this.getCapCanceller = null;
          if (!response.data) {
            return;
          }
          return unmarshaller.unmarshalString(response.data).value;
        });
      };

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnWpsService
       * @name gnWpsService#execute
       *
       * @description
       * Prints a WPS Execute message as XML to be posted to a WPS service.
       * Does a DescribeProcess call first
       *
       * @param {Object} processDescription from the wps service
       * @param {Object} inputs of the process; this must be an array of
       *  objects like so: { name: 'input_name', value: 'input value' }
       * @param {Object} output this object must hold output identifier &
       * mimeType as well as options such as storeExecuteResponse, lineage and
       * status
       * @return {string} XML message
       */
      this.printExecuteMessage = function(processDescription, inputs,
          output) {
        var me = this;
        var description = processDescription;

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
          if (input.complexData && data) {
            var mimeType = input.complexData._default.format.mimeType;
            request.value.dataInputs.input.push({
              identifier: {
                value: input.identifier.value
              },
              data: {
                complexData: {
                  mimeType: mimeType,
                  content: data
                }
              }
            });
          }
          if (input.boundingBoxData && data) {
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
          var inputName = input.identifier.value;

          // for each value for this input, add to message
          inputs.filter(function(inputValue) {
            return inputValue.name === inputName;
          }).forEach(function(inputValue) {
            setInputData(input, inputValue.value);
          });
        }

        // generate response document based on output info
        var responseDocument = {
          lineage: output.lineage || false,
          storeExecuteResponse: output.storeExecuteResponse || true,
          status: output.status || false,
          output: []
        };

        // output selection based on form control
        angular.forEach(description.processOutputs.output,
            function(descOutput) {
              if (descOutput.identifier.value === output.identifier) {
                responseDocument.output.push({
                  asReference: output.asReference !== undefined ?
                  output.asReference : descOutput.asReference,
                  mimeType: output.mimeType,
                  identifier: {
                    value: output.identifier
                  }
                });
              }
            }, {});

        request.value.responseForm = {
          responseDocument: responseDocument
        };

        return marshaller.marshalString(request);
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
       * @return {defer} promise
       */
      this.execute = function(uri, processId, inputs, responseDocument) {
        var defer = $q.defer();
        var me = this;

        this.describeProcess(uri, processId).then(
            function(data) {
              // generate the XML message from the description
              var description = data.processDescription[0];
              var message = me.printExecuteMessage(description, inputs,
              responseDocument);

              // do the post request
              $http.post(uri, message, {
                headers: {'Content-Type': 'application/xml'}
              }).then(function(data) {
                var response =
                unmarshaller.unmarshalString(data.data).value;
                defer.resolve(response);
              }, function(data) {
                defer.reject(data);
              });
            });

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
       * Returns true if the mime type matches a WMS service
       *
       * @param {object} response excecuteProcess response object.
       * @return {bool} true if WMS service
       */
      this.responseHasWmsService = function(response) {
        try {
          var mimeType = response.processOutputs.output[0].reference.mimeType;
          return this.WMS_MIMETYPE_REGEX.test(mimeType);
        } catch (e) {
          return false;
        }
      };

      /**
       * Returns an object if process description offers an output with WMS
       * The object hold the properties outputIdentifier and mimeType
       *
       * @param {object} processDesc describeProcess response object.
       * @param {string} outputIdentifier
       * @return {string} object with outputIdentifier and mimeType; null if no
       * matching mimeType
       */
      this.getProcessOutputWMSMimeType = function(processDesc) {
        var result = null;
        var me = this;
        try {
          var outputs = processDesc.processOutputs.output;
          outputs.forEach(function(output) {
            var outputId = output.identifier.value;
            var mimeTypes = output.complexOutput.supported.format;
            mimeTypes.forEach(function(mimeType) {
              if (!result && me.WMS_MIMETYPE_REGEX.test(mimeType.mimeType)) {
                result = {
                  mimeType: mimeType.mimeType,
                  outputIdentifier: outputId
                };
              }
            });
          });
        } catch (e) {
          console.warn('Failed parsing WPS process description: ', e);
        }
        return result;
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
       * @param {ol.layer.Base} parentLayer optional
       */
      this.extractWmsLayerFromResponse =
          function(response, map, parentLayer) {

        try {
          var ref = response.processOutputs.output[0].reference;
          gnMap.addWmsAllLayersFromCap(map, ref.href, true).
              then(function(layers) {
                layers.forEach(function(l) {
                  l.set('fromWps', true);
                  l.set('wpsParent', parentLayer);
                  map.addLayer(l);
                });
              });
        } catch (e) {
          console.warn('Error extracting WMS layers from response: ', e);
        }
      };

      /**
       * Returns a label normalized for the process description
       * Field used: `labels[currentLang]` or `label`
       * IF no label found, return nothing
       * @param {Object} wpsLink object holding the process link
       */
      this.getProcessLabel = function (wpsLink) {
        var currentLang = $translate.use();
        if (wpsLink.labels) {
          return wpsLink.labels[currentLang];
        } else if (wpsLink.label) {
          return wpsLink.label;
        } else {
          return null;
        }
      }
    }
  ]);

})();
