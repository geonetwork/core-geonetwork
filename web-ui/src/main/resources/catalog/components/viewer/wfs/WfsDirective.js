(function() {
  goog.provide('gn_wfs_directive');

  var module = angular.module('gn_wfs_directive', [
  ]);

  module.directive('gnWFS', [
    function() {

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

        }
      };
    }
  ]);
})();
