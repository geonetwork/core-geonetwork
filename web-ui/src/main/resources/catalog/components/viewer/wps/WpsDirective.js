(function() {
  goog.provide('gn_wps_directive');

  var module = angular.module('gn_wps_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWpsProcessForm
   * @restrict AE
   *
   * @description
   * The `gnWpsProcessForm` build up a HTML form from the describe process
   * response object (after call the describe process request).
   *
   * TODO: Add batch mode using md.privileges.batch
   * and md.privileges.batch.update services.
   *
   * TODO: User group only privilege
   */
  module.directive('gnWpsProcessForm', [
    'gnWpsService',
    'gnUrlUtils',
    '$timeout',
    function(gnWpsService, gnUrlUtils, $timeout) {

      var inputTypes = {
        string: 'text',
        float: 'number'
      };

      var parseKvpParams = function(str) {
        var escaper = function(match, p1) {
          return '=' + gnUrlUtils.encodeUriQuery(p1);
        };
        str = str.replace(/=\[([^&]*)\]/gi, escaper);

        var queryParams = gnUrlUtils.parseKeyValue(str);
        var defaults = {};
        for (var prop in queryParams) {
          if (queryParams.hasOwnProperty(prop)) {
            defaults[prop.toLowerCase()] = queryParams[prop];
          }
        }

        var parseDataInputs = function(value) {
          var datainputs = {};

          if (value === undefined) {
            return datainputs;
          }

          value.split(';').map(function(str) {
            var list = str.split('@');

            var input = list[0].split('=');
            var identifier = input[0];
            var value = input[1];
            list = list.slice(1);

            var attributes = {};
            list.map(function(attribute) {
              keyValue = attribute.split('=');
              attributes[keyValue[0].toLowerCase()] = keyValue[1];
            });

            datainputs[identifier] = {
              value: value,
              attributes: attributes
            };
          });
          return datainputs;
        };

        defaults.datainputs = parseDataInputs(defaults.datainputs);
        defaults.responsedocument = parseDataInputs(defaults.responsedocument);

        return defaults;
      };

      var toBool = function(str, defaultVal) {
        if (str === undefined) {
          return defaultVal;
        }
        return str.toLowerCase() === 'true';
      };

      return {
        restrict: 'AE',
        scope: {
          uri: '=',
          processId: '=',
          defaults: '=',
          map: '='
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/viewer/wps/partials/processform.html';
        },

        link: function(scope, element, attrs) {
          var defaults = parseKvpParams(scope.defaults);

          scope.describeState = 'sended';
          scope.executeState = '';

          gnWpsService.describeProcess(scope.uri, scope.processId)
          .then(
              function(response) {
                scope.describeState = 'succeeded';
                scope.describeResponse = response;

                if (response.processDescription != undefined) {
                  scope.processDescription = response.processDescription[0];
                  angular.forEach(scope.processDescription.dataInputs.input,
                      function(input) {
                        var value;
                        var defaultValue;
                        var datainput =
                          defaults.datainputs[input.identifier.value];
                        if (datainput != undefined) {
                          defaultValue = datainput.value;
                        }

                        if (input.literalData != undefined) {

                          // Input type
                          input.type =
                            inputTypes[input.literalData.dataType.value];

                          // Default value
                          if (input.literalData.defaultValue != undefined) {
                            value = input.literalData.defaultValue;
                          }
                          if (defaultValue != undefined) {
                            value = defaultValue;
                          }

                          // Format conversion
                          switch (input.literalData.dataType.value) {
                            case 'float':
                              value = parseFloat(value); break;
                            case 'string':
                              value = value || ''; break;
                          }
                          input.value = value;
                        }

                        if (input.boundingBoxData != undefined) {
                          input.value = '';
                          if (defaultValue) {
                            input.value = defaultValue.split(',')
                              .slice(0, 4).join(',');
                          }
                        }
                      }
                  );

                  angular.forEach(
                    scope.processDescription.processOutputs.output,
                      function(output) {
                        output.asReference = true;

                        var outputDefault =
                          defaults.responsedocument[output.identifier.value];
                        if (outputDefault) {
                          output.value = true;
                          var defaultAsReference =
                            outputDefault.attributes['asreference'];
                          if (defaultAsReference !== undefined) {
                            output.asReference = toBool(defaultAsReference);
                          }
                        }
                      }
                  );
                  var output = scope.processDescription.processOutputs.output;
                  if (output.length == 1) {
                    output[0].value = true;
                  }
                  scope.outputsVisible = true;

                  scope.responseDocument = {
                    lineage: toBool(defaults.lineage, false),
                    storeExecuteResponse:
                      toBool(defaults.storeexecuteresponse, false),
                    status: toBool(defaults.status, false)
                  };
                  scope.optionsVisible = true;
                }
              },
              function(response) {
                scope.describeState = 'failed';
                scope.describeResponse = response;
              }
              );

          scope.close = function() {
            element.remove();
          };

          scope.toggleOutputs = function() {
            scope.outputsVisible = !scope.outputsVisible;
          };

          scope.toggleOptions = function() {
            scope.optionsVisible = !scope.optionsVisible;
          };

          scope.submit = function() {
            scope.validation_messages = [];
            scope.exception = undefined;

            // Validate inputs
            var invalid = false;
            angular.forEach(scope.processDescription.dataInputs.input,
                function(input) {
                  input.invalid = undefined;
                  if (input.minOccurs > 0 && (input.value === null ||
                      input.value === '')) {
                    input.invalid = input.title.value + ' is mandatory';
                    invalid = true;
                  }
                });
            if (invalid) { return; }

            var inputs = scope.processDescription.dataInputs.input.reduce(
                function(o, v, i) {
                  o[v.identifier.value] = v.value;
                  return o;
                }, {});

            var outputs = [];
            angular.forEach(scope.processDescription.processOutputs.output,
                function(output) {
                  if (output.value == true) {
                    outputs.push({
                      asReference: output.asReference,
                      identifier: {
                        value: output.identifier.value
                      }
                    });
                  }
                }, {});
            scope.responseDocument.output = outputs;

            updateStatus = function(statusLocation) {
              gnWpsService.getStatus(statusLocation).then(
                  function(response) {
                    processResponse(response);
                  },
                  function(response) {
                    scope.executeState = 'failed';
                    scope.executeResponse = response;
                  }
              );
            };

            processResponse = function(response) {
              if (response.TYPE_NAME = 'OWS_1_1_0.ExceptionReport') {
                scope.executeState = 'finished';
              }
              if (response.TYPE_NAME = 'WPS_1_0_0.ExecuteResponse') {
                if (response.status != undefined) {
                  if (response.status.processAccepted != undefined ||
                      response.status.processPaused != undefined ||
                      response.status.processStarted != undefined) {
                    scope.executeState = 'pending';
                    scope.statusPromise = $timeout(function() {
                      updateStatus(response.statusLocation);
                    }, 1000, true);
                  }
                  if (response.status.ProcessSucceeded != undefined ||
                      response.status.ProcessFailed != undefined) {
                    scope.executeState = 'finished';
                  }
                }
              }
              scope.executeResponse = response;
            };

            scope.running = true;
            scope.executeState = 'sended';
            gnWpsService.execute(
                scope.uri,
                scope.processId,
                inputs,
                scope.responseDocument
            ).then(
                function(response) {
                  processResponse(response);
                },
                function(response) {
                  scope.executeState = 'failed';
                  scope.executeResponse = response;
                }
            ).finally (
                function() {
                  scope.running = false;
                });
          };

          scope.cancel = function() {
            if ($timeout.cancel(scope.statusPromise)) {
              scope.statusPromise = undefined;
              scope.executeState = 'cancelled';
            }
          };

          scope.responseDocumentStatusChanged = function() {
            if (scope.responseDocument.status == true) {
              scope.responseDocument.storeExecuteResponse = true;
            }
          };

        }
      };
    }
  ]);
})();
