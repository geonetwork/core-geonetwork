(function() {
  'use strict';
  goog.provide('inspire_ie9_select');

  var module = angular.module('inspire_ie9_select', []);

  module.directive('ie9Select', function() {
    return {
      scope: {
        field: "=",
        fieldAccessor: "=",
        options: '=',
        value: '@',
        title: '@'
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
         $scope.updateField = function (newVal) {
           if (newVal) {
             $scope.field[$scope.fieldAccessor] = newVal[$scope.value];
           }
         }
      },
      templateUrl: '../../catalog/components/edit/inspire/partials/select.html'
    };
  });

}());
