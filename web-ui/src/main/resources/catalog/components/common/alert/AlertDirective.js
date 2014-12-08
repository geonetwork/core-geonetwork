(function() {
  goog.provide('gn_alert');

  var module = angular.module('gn_alert', ['ngSanitize']);

  module.value('gnAlertValue', []);

  module.service('gnAlertService', [
    'gnAlertValue',
    '$timeout',
    function(gnAlertValue, $timeout) {

      var delay = 2000;
      this.addAlert = function(alert) {
        gnAlertValue.push(alert);

        $timeout(function() {
          gnAlertValue.splice(0, 1);
        }, delay);
      };
    }]);

  module.directive('gnAlertManager', [
    'gnAlertValue',
    function(gnAlertValue) {
      return {
        replace: true,
        restrict: 'A',
        templateUrl: '../../catalog/components/common/alert/' +
            'partials/alert.html',
        link: function(scope, element, attrs) {
          scope.alerts = gnAlertValue;
        }
      };
    }]);
})();
