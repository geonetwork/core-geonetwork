(function() {
  'use strict';
  goog.provide('inspire_date_picker_directive');
  goog.require('ui.bootstrap.datepicker');

  var module = angular.module('inspire_date_picker_directive', ['ui.bootstrap.datepicker']);

  /**
   *  Create a widget to handle date composed of
   *  a date input and a time input. It can only be
   *  used to create an ISO date. It hides the
   *  need of choosing from ISO type date or datetime.
   *
   *  It's also useful as html datetime input are not
   *  yet widely supported.
   */
  module.directive('inspireDatePicker', [ 'dateParser',
    function(dateParser) {
      return {
        restrict: 'A',
        scope: {
          dateObj: '='
        },
        templateUrl: '../../catalog/components/edit/inspire/datepicker/partials/datepicker.html',
        link: function($scope) {
          $scope.today = function() {
            $scope.dt = new Date();
          };
          $scope.dt = null;

          $scope.$watch('dateObj', function(newValue) {
            if (newValue && newValue.dateTagName === "gmd:DateTime") {
              $scope.format = 'yyyy-MM-dd mm:ss';
            } else {
              $scope.format = 'yyyy-MM-dd';
            }
            if (newValue) {
              $scope.dt = dateParser.parse(newValue.date, $scope.format);
            }
          });
          $scope.$watch('dt', function(newValue) {
            if (newValue) {
              var isoDate = newValue.toISOString();
              if ($scope.dateObj.dateTagName === "gmd:DateTime") {
                $scope.dateObj.date = isoDate;
              } else {
                $scope.dateObj.dateTagName = "gmd:Date";
                $scope.dateObj.date = isoDate.substr(0, isoDate.indexOf('T'));
              }
            } else {
              $scope.dateObj.dateTagName = "gmd:Date";
              $scope.dateObj.date = '';
            }

          });

          $scope.clear = function () {
            $scope.dt = null;
          };

          $scope.open = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
          };

          $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
          };

          $scope.initDate = new Date('2016-15-20');
        }
      };
    }]);
}());
