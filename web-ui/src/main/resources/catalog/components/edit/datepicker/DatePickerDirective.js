(function() {
  goog.provide('gn_date_picker_directive');

  var module = angular.module('gn_date_picker_directive', []);

  /**
   *  Create a widget to handle date composed of
   *  a date input and a time input. It can only be
   *  used to create an ISO date. It hides the
   *  need of choosing from ISO type date or datetime.
   *
   *  It's also useful as html datetime input are not
   *  yet widely supported.
   */
  module.directive('gnDatePicker', ['$http', '$rootScope',
    function($http, $rootScope) {

      return {
        restrict: 'A',
        scope: {
          value: '@gnDatePicker',
          label: '@',
          elementName: '@',
          elementRef: '@',
          id: '@',
          tagName: '@',
          indeterminatePosition: '@'
        },
        templateUrl: '../../catalog/components/edit/datepicker/partials/' +
            'datepicker.html',
        link: function(scope, element, attrs) {
          // Check if browser support date type or not to
          // HTML date and time input types.
          // If not datetimepicker.js is used (it will not
          // support year or month only mode in this case)
          scope.dateTypeSupported = Modernizr.inputtypes.date;

          var namespaces = {
            gco: 'http://www.isotc211.org/2005/gco',
            gml: 'http://www.opengis.net/gml'
          };
          // Format date when datetimepicker is used.
          scope.formatFromDatePicker = function(date) {
            var format = 'YYYY-MM-DDTHH:mm:ss';
            var dateTime = moment(date);
            scope.dateInput = dateTime.format(format);
          };

          scope.mode = scope.year = scope.month = scope.time =
              scope.date = scope.dateDropDownInput = '';
          scope.withIndeterminatePosition =
              attrs.indeterminatePosition !== undefined;

          // Default date is empty
          // Compute mode based on date length. The format
          // is always ISO YYYY-MM-DDTHH:mm:ss
          if (!scope.value) {
            scope.value = '';
          } else if (scope.value.length === 4) {
            scope.year = parseInt(scope.value);
            scope.mode = 'year';
          } else if (scope.value.length === 7) {
            scope.month = scope.value;
            scope.mode = 'month';
          } else {
            var isDateTime = scope.value.indexOf('T') !== -1;
            var tokens = scope.value.split('T');
            scope.date = isDateTime ? tokens[0] : scope.value;
            scope.time = isDateTime ? tokens[1] : '';
          }
          if (scope.dateTypeSupported !== true) {
            scope.dateInput = scope.value;
            scope.dateDropDownInput = scope.value;
          }

          scope.setMode = function(mode) {
            scope.mode = mode;
          };

          var resetDateIfNeeded = function() {
            // Reset date if indeterminate position is now
            // or unknows.
            if (scope.withIndeterminatePosition &&
                (scope.indeterminatePosition === 'now' ||
                scope.indeterminatePosition === 'unknown')) {
              scope.dateInput = '';
              scope.date = '';
              scope.year = '';
              scope.month = '';
              scope.time = '';
            }
          };

          // Build xml snippet based on input date.
          var buildDate = function() {
            var tag = scope.tagName !== undefined ?
                scope.tagName : 'gco:Date';
            var namespace = tag.split(':')[0];

            if (scope.dateTypeSupported !== true) {

              if (scope.dateInput === undefined) {
                return;
              } else {
                tag = scope.tagName !== undefined ? scope.tagName :
                    (scope.dateInput.indexOf('T') === -1 ?
                    'gco:Date' : 'gco:DateTime');
              }
              scope.dateTime = scope.dateInput;
            } else if (scope.mode === 'year') {
              scope.dateTime = scope.year;
            } else if (scope.mode === 'month') {
              scope.dateTime = scope.month;
            } else if (scope.time) {
              tag = scope.tagName !== undefined ?
                  scope.tagName : 'gco:DateTime';
              var time = scope.time;
              // TODO: Set seconds, Timezone ?
              scope.dateTime = scope.date;

              // Add seconds if not set
              if (time.length === 5) {
                time += ':00';
              }
              scope.dateTime += 'T' + time;
            } else {
              scope.dateTime = scope.date;
            }
            if (tag === '') {
              scope.xmlSnippet = scope.dateTime;
            } else {
              if (scope.dateTime != '' || scope.indeterminatePosition != '') {
                var attribute = '';
                if (scope.withIndeterminatePosition &&
                    scope.indeterminatePosition !== '') {
                  attribute = ' indeterminatePosition="' +
                      scope.indeterminatePosition + '"';
                }
                scope.xmlSnippet = '<' + tag +
                    ' xmlns:' + namespace + '="' + namespaces[namespace] + '"' +
                    attribute + '>' +
                    scope.dateTime + '</' + tag + '>';
              } else {
                scope.xmlSnippet = '';
              }
            }
          };

          scope.$watch('date', buildDate);
          scope.$watch('time', buildDate);
          scope.$watch('year', buildDate);
          scope.$watch('month', buildDate);
          scope.$watch('dateInput', buildDate);
          scope.$watch('indeterminatePosition', buildDate);
          scope.$watch('indeterminatePosition', resetDateIfNeeded);
          scope.$watch('xmlSnippet', function() {
            if (scope.id) {
              $(scope.id).val(scope.xmlSnippet);
              $(scope.id).change();
            }
          });

          buildDate();
        }
      };
    }]);
})();
