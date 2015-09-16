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
    '$timeout',
    function(gnWpsService, $timeout) {

      var inputTypes = {
        string: 'text',
        float: 'number'
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
          var defaults = scope.defaults || {};

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
                        if (input.literalData) {
                          // Input type
                          input.type = inputTypes[input.literalData.dataType.value];

                          // Default value
                          var value = undefined;
                          if (input.literalData.defaultValue != undefined) {
                            value = input.literalData.defaultValue;
                          }
                          if (defaults[input.identifier.value] != undefined) {
                            value = defaults[input.identifier.value];
                          }
                          switch (input.literalData.dataType.value) {
                            case 'float':
                              value = parseFloat(value); break;
                            case 'string':
                              value = value || ''; break;
                          }
                          input.value = value;
                        }
                        if (input.boundingBoxData) {
                          input.value = '';
                        }
                      }
                  );

                  angular.forEach(scope.processDescription.processOutputs.output,
                      function(output) {
                        output.value = true;
                        output.asReference = true;
                      }
                  );
                  scope.outputsVisible = false;

                  scope.responseDocument = {
                    lineage: false,
                    storeExecuteResponse: true,
                    status: false
                  };

                  scope.optionsVisible = false;
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
                    scope.statusPromise = $timeout(function() { updateStatus(response.statusLocation); }, 1000, true);
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
