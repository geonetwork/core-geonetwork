(function() {
  goog.provide('gn_field_duration_directive');

  var module = angular.module('gn_field_duration_directive', []);

  /**
     *  Create a widget to handle editing of xsd:duration elements.
     *
     *  Format: PnYnMnDTnHnMnS
     *
     *  *  P indicates the period (required)
     *  * nY indicates the number of years
     *  * nM indicates the number of months
     *  * nD indicates the number of days
     *  * T indicates the start of a time section
     *       (required if you are going to specify hours, minutes, or seconds)
     *  * nH indicates the number of hours
     *  * nM indicates the number of minutes
     *  * nS indicates the number of seconds
     */
  module.directive('gnFieldDuration', ['$http', '$rootScope',
    function($http, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          value: '@gnFieldDuration',
          label: '@label',
          ref: '@ref'
        },
        templateUrl: '../../catalog/components/edit/fieldduration/partials/' +
            'fieldduration.html',
        link: function(scope, element, attrs) {

          var buildDuration = function() {
            var duration = [scope.sign === true ? '-' : '',
              'P',
              scope.years, 'Y',
              scope.monthes, 'M',
              scope.days, 'D',
              'T',
              scope.hours, 'H',
              scope.minutes, 'M',
              scope.secondes, 'S'];
            scope.value = duration.join('');
          };


          // Set the default value
          if (scope.value === undefined) {
            scope.value = 'P0Y0M0DT0H0M0S';
          }

          // Extract duration values
          var tokens = scope.value.split(/[PYMDTHMS]/);

          scope.sign = tokens[0] === '-';
          scope.years = parseInt(tokens[1]);
          scope.monthes = parseInt(tokens[2]);
          scope.days = parseInt(tokens[3]);
          scope.hours = parseInt(tokens[5]);
          scope.minutes = parseInt(tokens[6]);
          scope.secondes = parseInt(tokens[7]);

          // Compute duration when any components change
          angular.forEach(['sign', 'years', 'monthes', 'days',
                           'hours', 'minutes', 'secondes'],
          function(value) {
            scope.$watch(value, buildDuration);
          });

        }
      };
    }]);
})();
