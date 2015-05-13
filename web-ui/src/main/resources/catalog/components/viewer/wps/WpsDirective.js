(function() {
  goog.provide('gn_wps_directive');

  var module = angular.module('gn_wps_directive', [
  ]);

  module.directive('gnWpsProcessForm', [
    'gnWpsService',
    function(gnWpsService) {

      var inputTypeMapping = {
        string: 'text',
        float: 'number'
      };

      var defaultValue = function(literalData) {
        var value = undefined;
        if (literalData.defaultValue != undefined) {
          value = literalData.defaultValue;
        }
        if (literalData.dataType.value == 'float') {
          value = parseFloat(value);
        }
        if (literalData.dataType.value == 'string') {
          value = value || '';
        }
        return value;
      };

      return {
        restrict: 'AE',
        scope: {
          uri: '=',
          processId: '='
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/viewer/wps/partials/processform.html';
        },

        link: function(scope, element, attrs) {

          scope.status = 'loading';
          gnWpsService.describeProcess(scope.uri, scope.processId)
          .then(
              function(data) {
                scope.processDescription = data.processDescription[0];
                scope.title = scope.processDescription.title.value;
                scope.inputs = [];
                angular.forEach(scope.processDescription.dataInputs.input,
                  function(input) {
                    scope.inputs.push({
                      name: input.identifier.value,
                      title: input.title.value,
                      type: inputTypeMapping[input.literalData.dataType.value],
                      value: defaultValue(input.literalData),
                      required: input.minOccurs == 1 ? true : false
                    });
                  }
                );
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
            var inputs = scope.inputs.reduce(function(o, v, i) {
              o[v.name] = v.value.toString();
              return o;
            }, {});
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
                  scope.status = 'error';
                }
            );
          };
        }
      };
    }
  ]);
})();
