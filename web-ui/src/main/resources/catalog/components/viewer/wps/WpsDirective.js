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
    function(gnWpsService) {

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

          scope.status = 'loading';
          gnWpsService.describeProcess(scope.uri, scope.processId)
          .then(
              function(data) {
                scope.processDescription = data.processDescription[0];
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
                  });
                scope.status = 'loaded';
              },
              function(data) {
                scope.exception = data;
                scope.status = 'error';
              }
              );

          scope.close = function() {
            element.remove();
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

            scope.running = true;
            gnWpsService.execute(
                scope.uri,
                scope.processId,
                inputs,
                scope.processDescription.processOutputs.
                    output[0].identifier.value,
                false
            ).then(
                function(data) {
                  window.open(data);
                },
                function(data) {
                  scope.exception = data;
                }
            ).finally (
                function() {
                  scope.running = false;
                });
          };
        }
      };
    }
  ]);
})();
