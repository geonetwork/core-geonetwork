(function() {
  goog.provide('gn_wps_service');

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

  module.service('gnWpsService', [
    '$http',
    'gnOwsCapabilities',
    'gnUrlUtils',
    'gnGlobalSettings',
    '$q',
    function($http, gnOwsCapabilities, gnUrlUtils, gnGlobalSettings, $q) {

      this.proxyUrl = function(url) {
        return gnGlobalSettings.proxyUrl + encodeURIComponent(url);
      };

      this.describeProcess = function(uri, processId) {
        url = gnOwsCapabilities.mergeDefaultParams(uri, {
          service: 'WPS',
          version: '1.0.0',
          request: 'DescribeProcess',
          identifier: processId
        });

        //send request and decode result
        if (gnUrlUtils.isValid(url)) {
          var defer = $q.defer();

          var proxyUrl = this.proxyUrl(url);
          $http.get(proxyUrl, {
            cache: true
          }).then(
              function(data) {
                var response = unmarshaller.unmarshalString(data.data).value;
                if (response.exception != undefined) {
                  defer.reject({msg: 'wpsDescribeProcessFailed',
                    owsExceptionReport: response});
                }
                else {
                  defer.resolve(response);
                }
              },
              function(data) {
                defer.reject({msg: 'wpsDescribeProcessFailed',
                  httpResponse: data});
              }
          );

          return defer.promise;
        }
      };

      this.execute = function(uri, processId, inputs, output) {
        var defer = $q.defer();

        var me = this;

        this.describeProcess(uri, processId).then(
            function(data) {
              var description = data.processDescription[0];

              var url = me.proxyUrl(uri);
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
                var inputValue;
                request.value.dataInputs.input.push({
                  identifier: {
                    value: input.identifier.value
                  },
                  data: {
                    literalData: {
                      value: data
                    }
                  }
                });
              };

              for (i = 0, ii = description.dataInputs.input.length;
                   i < ii; ++i) {
                input = description.dataInputs.input[i];
                if (inputs[input.identifier.value] !== undefined) {
                  setInputData(input, inputs[input.identifier.value]);
                }
              }

              var getOutputIndex = function(outputs, identifier) {
                var output;
                if (identifier) {
                  for (var i = outputs.length - 1; i >= 0; --i) {
                    if (outputs[i].identifier.value === identifier) {
                      output = i;
                      break;
                    }
                  }
                } else {
                  output = 0;
                }
                return output;
              };

              var setResponseForm = function(options) {
                options = options || {};
                var output =
                    description.processOutputs.output[options.outputIndex || 0];
                request.value.responseForm = {
                  responseDocument: {
                    lineage: false,
                    storeExecuteResponse: true,
                    status: false,
                    output: [{
                      asReference: true,
                      identifier: {
                        value: output.identifier.value
                      }
                    }]
                  }
                };
              };

              var outputIndex = getOutputIndex(
                  description.processOutputs.output, output);
              setResponseForm({outputIndex: outputIndex});

              var body = marshaller.marshalString(request);

              $http.post(url, body, {
                headers: {'Content-Type': 'application/xml'}
              }).then(
                  function(data) {
                    var response =
                        unmarshaller.unmarshalString(data.data).value;
                    var status = response.status;
                    if (status.processFailed != undefined) {
                      defer.reject({msg: 'wpsExecuteFailed',
                        owsExceptionReport:
                            status.processFailed.exceptionReport});
                    } else {
                      var url =
                          response.processOutputs.output[0].reference.href;
                      defer.resolve(url);
                    }
                  },
                  function(data) {
                    defer.reject({msg: 'wpsExecuteFailed',
                      httpResponse: data});
                  }
              );

            },
            function(data) {
              defer.reject(data);
            }
        );

        return defer.promise;
      };
    }
  ]);
})();
